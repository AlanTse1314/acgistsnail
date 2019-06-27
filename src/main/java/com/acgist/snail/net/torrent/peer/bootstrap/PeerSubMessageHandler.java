package com.acgist.snail.net.torrent.peer.bootstrap;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.IMessageHandler;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.bootstrap.PeerConnect;
import com.acgist.snail.net.torrent.bootstrap.PeerConnectGroup;
import com.acgist.snail.net.torrent.bootstrap.PeerLauncher;
import com.acgist.snail.net.torrent.peer.bootstrap.dht.DhtExtensionMessageHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.ltep.ExtensionMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.BitfieldUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Peer消息处理</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0003.html</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0004.html</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0009.html</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0010.html</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0011.html</p>
 * <pre>
 * D = 目前正在下载（有需要的文件部份且没有禁止连接）
 * d = 客户端请求下载，但用户拒绝传输（有需要的文件部份但连接被禁止）
 * U = 目前正在上传（需要的文件部份且没有禁止连接）
 * u = 用户请求客户端上传，但客户端拒绝（有需要的文件部份但连接被禁止）
 * O = 刷新并接受禁止连接的用户
 * S = 用户被拒（一段时间没有传送任何数据的用户，一般是60秒）
 * I = 用户为传入连接
 * K = 客户端没有用户需要的文件部份
 * ? = 用户没有客户端需要的文件部份
 * X = 通过Peer Exchange（PEX）获取的用户列表所包含的用户或IPv6用户通知客户端其IPv4地址
 * H = 通过DHT连接的用户
 * E = 用户正使用协议加密连接（全部流量）
 * e = 用户正使用协议加密连接（握手）
 * P = 用户正使用uTP连接
 * L = 用户是本地的（通过网络广播或是保留的本地IP范围发现）
 * </pre>
 * 
 * TODO：加密
 * TODO：实现流水线
 * 
 * @author acgist
 * @since 1.1.0
 */
public class PeerSubMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerSubMessageHandler.class);
	
	private volatile boolean handshakeSed = false; // 发送握手
	private volatile boolean handshakeRcv = false; // 接收握手

	/**
	 * 连入客户端
	 */
	private PeerConnect peerConnect;
	/**
	 * 请求客户端
	 */
	private PeerLauncher peerLauncher;
	
	private PeerSession peerSession;
	private TorrentSession torrentSession;
	
	private ExtensionMessageHandler extensionMessageHandler;
	private DhtExtensionMessageHandler dhtExtensionMessageHandler;
	
	/**
	 * 消息代理
	 */
	private IMessageHandler messageHandler;
	
	private PeerSubMessageHandler() {
	}

	private PeerSubMessageHandler(PeerSession peerSession, TorrentSession torrentSession) {
		init(peerSession, torrentSession, PeerConfig.HANDSHAKE_RESERVED);
	}
	
	public static final PeerSubMessageHandler newInstance() {
		return new PeerSubMessageHandler();
	}
	
	public static final PeerSubMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession) {
		return new PeerSubMessageHandler(peerSession, torrentSession);
	}

	/**
	 * 初始化
	 */
	private void init(PeerSession peerSession, TorrentSession torrentSession, byte[] reserved) {
		peerSession.reserved(reserved);
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.extensionMessageHandler = ExtensionMessageHandler.newInstance(this.peerSession, this.torrentSession, this);
		this.dhtExtensionMessageHandler = DhtExtensionMessageHandler.newInstance(this.peerSession, torrentSession.dhtLauncher(), this);
	}

	/**
	 * 初始化：客户端连接，加入到Peer列表。
	 */
	private boolean init(String infoHashHex, byte[] peerId, byte[] reserved) {
		if(ArrayUtils.equals(PeerService.getInstance().peerId(), peerId)) {
			LOGGER.debug("Peer连接失败，PeerId一致");
			return false;
		}
		final TorrentSession torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
		if(torrentSession == null) {
			LOGGER.warn("Peer连接失败，不存在的种子信息");
			return false;
		}
		if(!torrentSession.uploadable()) {
			LOGGER.warn("Peer连接失败，Torrent任务不可上传");
			return false;
		}
		final InetSocketAddress socketAddress = remoteSocketAddress();
		if(socketAddress == null) {
			LOGGER.warn("Peer连接失败，获取远程Peer信息失败");
			return false;
		}
		final PeerSession peerSession = PeerManager.getInstance().newPeerSession(
			infoHashHex,
			torrentSession.statistics(),
			socketAddress.getHostString(),
			null,
			PeerConfig.SOURCE_CONNECT);
		final PeerConnectGroup peerConnectGroup = torrentSession.peerConnectGroup();
		final PeerConnect peerConnect = peerConnectGroup.newPeerConnect(peerSession, this);
		if(peerConnect != null) {
			this.peerConnect = peerConnect;
			peerSession.peerConnect(this.peerConnect);
			init(peerSession, torrentSession, reserved);
			return true;
		} else {
			return false;
		}
	}
	
	public PeerSubMessageHandler messageHandler(IMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
		return this;
	}
	
	/**
	 * 是否被握手
	 */
	public boolean handshake() {
		return this.handshakeRcv;
	}
	
	/**
	 * 处理单条消息
	 */
	public void oneMessage(final ByteBuffer buffer) {
		buffer.flip();
		if(!this.handshakeRcv) {
			handshake(buffer);
		} else {
			final byte typeValue = buffer.get();
			final PeerConfig.Type type = PeerConfig.Type.valueOf(typeValue);
			if(type == null) {
				LOGGER.warn("不支持的Peer消息类型：{}", typeValue);
				return;
			}
			LOGGER.debug("Peer消息类型：{}", type);
			switch (type) {
			case choke:
				choke(buffer);
				break;
			case unchoke:
				unchoke(buffer);
				break;
			case interested:
				interested(buffer);
				break;
			case notInterested:
				notInterested(buffer);
				break;
			case have:
				have(buffer);
				break;
			case bitfield:
				bitfield(buffer);
				break;
			case request:
				request(buffer);
				break;
			case piece:
				piece(buffer);
				break;
			case cancel:
				cancel(buffer);
				break;
			case dht:
				dht(buffer);
				break;
			case extension:
				extension(buffer);
				break;
			}
		}
	}

	/**
	 * <p>发送握手消息</p>
	 * <p>消息格式：pstrlen pstr reserved info_hash peer_id<br>
	 * pstrlen：pstr的长度：19<br>
	 * pstr：BitTorrent协议的关键字：BitTorrent protocol<br>
	 * reserved：8字节，用于扩展BT协议，一般都设置：0<br>
	 * info_hash：info_hash<br>
	 * peer_id：peer_id
	 * </p>
	 */
	public void handshake(PeerLauncher peerLauncher) {
		LOGGER.debug("握手");
		this.handshakeSed = true;
		this.peerLauncher = peerLauncher;
		if(this.peerSession != null && this.peerLauncher != null) {
			this.peerSession.peerLauncher(this.peerLauncher);
		}
		final ByteBuffer buffer = ByteBuffer.allocate(PeerConfig.HANDSHAKE_LENGTH);
		buffer.put((byte) PeerConfig.HANDSHAKE_NAME_BYTES.length);
		buffer.put(PeerConfig.HANDSHAKE_NAME_BYTES);
		buffer.put(PeerConfig.HANDSHAKE_RESERVED);
		buffer.put(this.torrentSession.infoHash().infoHash());
		buffer.put(PeerService.getInstance().peerId());
		this.send(buffer);
	}
	
	/**
	 * <p>处理握手消息</p>
	 * <p>服务端：初始化、握手、解除阻塞。</p>
	 * <p>客户端：设置id。</p>
	 * <p>然后发送扩展消息、DHT消息、交换位图。</p>
	 */
	private void handshake(ByteBuffer buffer) {
		LOGGER.debug("被握手");
		if(buffer.remaining() != PeerConfig.HANDSHAKE_LENGTH) {
			LOGGER.warn("握手消息格式错误（消息长度）：{}", buffer.remaining());
			this.close();
			return;
		}
		this.handshakeRcv = true;
		final boolean server = !this.handshakeSed; // 是否是服务方
		final byte length = buffer.get();
		if(length <= 0) {
			LOGGER.warn("握手消息格式错误（协议长度）：{}", length);
			this.close();
			return;
		}
		final byte[] names = new byte[length];
		buffer.get(names);
		final String name = new String(names);
		if(!PeerConfig.HANDSHAKE_NAME.equals(name)) {
			LOGGER.warn("下载协议错误：{}", name);
			this.close();
			return;
		}
		final byte[] reserved = new byte[PeerConfig.RESERVED_LENGTH];
		buffer.get(reserved);
		final byte[] infoHash = new byte[InfoHash.INFO_HASH_LENGTH];
		buffer.get(infoHash);
		final String infoHashHex = StringUtils.hex(infoHash);
		final byte[] peerId = new byte[PeerConfig.PEER_ID_LENGTH];
		buffer.get(peerId);
		if(server) {
			final boolean ok = init(infoHashHex, peerId, reserved);
			if(ok) {
				handshake((PeerLauncher) null); // 握手
			} else {
				this.close();
				return;
			}
		}
		this.peerSession.id(peerId);
		extension(); // 发送扩展
		dht(); // 发送DHT端口
		bitfield(); // 交换位图
		if(server) {
			unchoke();
		}
	}

	/**
	 * <p>发送心跳消息</p>
	 * <p>
	 * 4字节：消息持久：len=0000<br>
	 * 只有消息长度，没有消息编号和负载
	 * </p>
	 */
	public void keepAlive() {
		pushMessage(null, null);
	}
	
	/**
	 * <p>发送阻塞消息</p>
	 * <p>5字节：len=0001 id=0</p>
	 */
	public void choke() {
		LOGGER.debug("阻塞");
		this.peerSession.amChoke();
		pushMessage(PeerConfig.Type.choke, null);
	}

	/**
	 * <p>处理阻塞消息</p>
	 */
	private void choke(ByteBuffer buffer) {
		LOGGER.debug("被阻塞");
		this.peerSession.peerChoke();
		// 不释放资源暂时，让系统自动优化剔除
//		if(this.peerLauncher != null) {
//			this.peerLauncher.release();
//		}
	}
	
	/**
	 * <p>发送解除阻塞消息</p>
	 * <p>
	 * 5字节：len=0001 id=1<br>
	 * 解除阻塞
	 * </p>
	 */
	public void unchoke() {
		LOGGER.debug("解除阻塞");
		this.peerSession.amUnchoke();
		pushMessage(PeerConfig.Type.unchoke, null);
	}
	
	/**
	 * <p>处理解除阻塞消息</p>
	 * <p>被解除阻塞后开始发送下载请求。</p>
	 */
	private void unchoke(ByteBuffer buffer) {
		LOGGER.debug("被解除阻塞");
		this.peerSession.peerUnchoke();
		if(this.torrentSession.downloadable()) {
			if(this.peerLauncher != null) {
				this.peerLauncher.download(); // 开始下载
			}
		}
	}
	
	/**
	 * <p>发送感兴趣消息</p>
	 * <p>
	 * 5字节：len=0001 id=2<br>
	 * 收到have消息时，客户端对Peer感兴趣
	 * </p>
	 */
	public void interested() {
		LOGGER.debug("感兴趣");
		this.peerSession.amInterested();
		pushMessage(PeerConfig.Type.interested, null);
	}

	/**
	 * <p>处理感兴趣消息</p>
	 */
	private void interested(ByteBuffer buffer) {
		LOGGER.debug("被感兴趣");
		this.peerSession.peerInterested();
	}

	/**
	 * <p>发送不感兴趣消息</p>
	 * <p>
	 * 5字节：len=0001 id=3<br>
	 * 客户端对Peer不感兴趣
	 * </p>
	 */
	public void notInterested() {
		LOGGER.debug("不感兴趣");
		this.peerSession.amNotInterested();
		pushMessage(PeerConfig.Type.notInterested, null);
	}

	/**
	 * <p>处理不感兴趣消息</p>
	 */
	private void notInterested(ByteBuffer buffer) {
		LOGGER.debug("被不感兴趣");
		this.peerSession.peerNotInterested();
	}

	/**
	 * <p>发送have消息</p>
	 * <p>
	 * 5字节：len=0005 id=4 piece_index<br>
	 * piece index：piece下标，每当客户端下载完piece，发出have消息告诉所有与客户端连接的Peer
	 * </p>
	 */
	public void have(int index) {
		if(!this.torrentSession.uploadable()) {
			return;
		}
		LOGGER.debug("发送have消息：{}", index);
		pushMessage(PeerConfig.Type.have, ByteBuffer.allocate(4).putInt(index).array());
	}

	/**
	 * <p>处理have消息</p>
	 */
	private void have(ByteBuffer buffer) {
		if(!this.torrentSession.uploadable()) {
			return;
		}
		final int index = buffer.getInt();
		LOGGER.debug("收到have消息：{}", index);
		this.peerSession.piece(index);
		if(this.torrentSession.havePiece(index)) { // 已有=不感兴趣
			notInterested();
		} else { // 没有=感兴趣
			interested();
		}
	}

	/**
	 * <p>发送位图消息</p>
	 * <p>
	 * 长度不固定：len=0001+X id=5 bitfield<br>
	 * 交换位图：X=bitfield.length，握手后交换位图，每个piece占一位
	 * </p>
	 */
	public void bitfield() {
		if(!this.torrentSession.uploadable()) {
			return;
		}
		final BitSet pieces = this.torrentSession.pieces();
		LOGGER.debug("发送位图：{}", pieces);
		final int pieceSize = this.torrentSession.torrent().getInfo().pieceSize();
		pushMessage(PeerConfig.Type.bitfield, BitfieldUtils.toBytes(pieceSize, pieces));
	}
	
	/**
	 * <p>处理位图消息</p>
	 */
	private void bitfield(ByteBuffer buffer) {
		if(!this.torrentSession.uploadable()) {
			return;
		}
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BitSet pieces = BitfieldUtils.toBitSet(bytes);
		this.peerSession.pieces(pieces);
		LOGGER.debug("收到位图：{}", pieces);
		final BitSet notHave = new BitSet();
		notHave.or(pieces);
		notHave.andNot(this.torrentSession.pieces());
		LOGGER.debug("感兴趣位图：{}", notHave);
		if(notHave.cardinality() == 0) {
			notInterested();
		} else {
			interested();
		}
	}

	/**
	 * <p>发送request消息</p>
	 * <p>
	 * 13字节：len=0013 id=6 index begin length<br>
	 * index：piece的索引<br>
	 * begin：piece内的偏移<br>
	 * length：请求Peer发送的数据的长度<br>
	 * 当客户端收到Peer的unchoke请求后即可构建request消息，一般交换数据是以slice（长度16KB的块）为单位的<br>
	 * </p>
	 */
	public void request(int index, int begin, int length) {
		if(!this.torrentSession.downloadable()) {
			return;
		}
		if(this.peerSession.isPeerChocking()) {
			return; // 被阻塞不发送请求
		}
		LOGGER.debug("发送请求：{}-{}-{}", index, begin, length);
		ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.putInt(length);
		pushMessage(PeerConfig.Type.request, buffer.array());
	}

	/**
	 * <p>处理request消息</p>
	 */
	private void request(ByteBuffer buffer) {
		if(!this.torrentSession.uploadable()) {
			return;
		}
		if(this.peerSession.isAmChocking()) { // 被阻塞不操作
			return;
		}
		final int index = buffer.getInt();
		final int begin = buffer.getInt();
		final int length = buffer.getInt();
		LOGGER.debug("收到请求：{}-{}-{}", index, begin, length);
		if(this.torrentSession.havePiece(index)) {
			try {
				final byte[] bytes = this.torrentSession.read(index, begin, length);
				piece(index, begin, bytes);
			} catch (NetException e) {
				LOGGER.error("处理请求异常", e);
			}
		}
	}

	/**
	 * <p>发送piece消息</p>
	 * <p>
	 * 长度不固定：len=0009+X id=7 index begin block<br>
	 * piece消息：X=block长度（一般为16KB），收到request消息，如果没有Peer未被阻塞，且存在slice，则返回数据
	 * </p>
	 */
	public void piece(int index, int begin, byte[] bytes) {
		if(!this.torrentSession.uploadable()) {
			return;
		}
		if(bytes == null) {
			return;
		}
		LOGGER.debug("发送响应：{}-{}", index, begin);
		this.peerSession.upload(bytes.length); // 上传
		ByteBuffer buffer = ByteBuffer.allocate(8 + bytes.length);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.put(bytes);
		pushMessage(PeerConfig.Type.piece, buffer.array());
	}

	/**
	 * <p>处理piece消息</p>
	 */
	private void piece(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			return;
		}
		final int index = buffer.getInt();
		final int begin = buffer.getInt();
		LOGGER.debug("收到响应：{}-{}", index, begin);
		final int remaining = buffer.remaining();
		byte[] bytes = null;
		if(remaining > 0) {
			bytes = new byte[remaining];
			buffer.get(bytes);
		}
		if(this.peerLauncher != null) {
			this.peerLauncher.piece(index, begin, bytes);
		}
	}

	/**
	 * <p>发送cancel消息</p>
	 * <p>
	 * 13字节：len=0013 id=8 index begin length<br>
	 * 与request作用相反，取消下载
	 * </p>
	 */
	public void cancel(int index, int begin, int length) {
		ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.putInt(length);
		pushMessage(PeerConfig.Type.cancel, buffer.array());
	}
	
	/**
	 * <p>处理cancel消息</p>
	 */
	private void cancel(ByteBuffer buffer) {
		// TODO：不处理
	}
	
	/**
	 * <p>发送DHT消息</p>
	 * <p>
	 * 3字节：len=0003 id=9 listen-port<br>
	 * listen-port：两字节<br>
	 * 支持DHT的客户端使用，指明DHT监听的端口
	 * </p>
	 */
	public void dht() {
		if(this.peerSession.supportDhtProtocol()) {
			LOGGER.debug("发送DHT消息");
			this.dhtExtensionMessageHandler.port();
		}
	}
	
	/**
	 * <p>处理DHT消息</p>
	 */
	private void dht(ByteBuffer buffer) {
		this.dhtExtensionMessageHandler.onMessage(buffer);
	}

	/**
	 * <p>发送扩展消息</p>
	 * <p>扩展消息：len=unknow id=20 消息</p>
	 */
	public void extension() {
		if(this.peerSession.supportExtensionProtocol()) {
			LOGGER.debug("发送扩展消息");
			this.extensionMessageHandler.handshake();
		}
	}
	
	/**
	 * <p>处理扩展信息</p>
	 */
	private void extension(ByteBuffer buffer) {
		LOGGER.debug("收到扩展消息");
		this.extensionMessageHandler.onMessage(buffer);
	}
	
	/**
	 * 发送扩展信息：Pex
	 */
	public void exchange(byte[] bytes) {
		this.extensionMessageHandler.exchange(bytes);
	}
	
	/**
	 * 发送消息
	 */
	public void pushMessage(PeerConfig.Type type, byte[] payload) {
		this.send(buildMessage(type, payload));
	}
	
	/**
	 * <p>创建消息</p>
	 * <p>
	 * 消息格式：length_prefix message_ID payload<br>
	 * length prefix：4字节：message id和payload的长度和<br>
	 * message id：1字节：指明消息的编号<br>
	 * payload：消息内容
	 * </p>
	 */
	private ByteBuffer buildMessage(PeerConfig.Type type, byte[] payload) {
		final Byte id = type == null ? null : type.value();
		int capacity = 0;
		if(id != null) {
			capacity += 1;
		}
		if(payload != null) {
			capacity += payload.length;
		}
		final ByteBuffer buffer = ByteBuffer.allocate(capacity + 4); // +4 = length prefix
		buffer.putInt(capacity);
		if(id != null) {
			buffer.put(id);
		}
		if(payload != null) {
			buffer.put(payload);
		}
		return buffer;
	}
	
	public void close() {
		this.messageHandler.close();
	}
	
	public boolean available() {
		return this.messageHandler.available();
	}
	
	private void send(ByteBuffer buffer) {
		try {
			this.messageHandler.send(buffer);
		} catch (Exception e) {
			LOGGER.error("Peer消息发送异常", e);
		}
	}

	private InetSocketAddress remoteSocketAddress() {
		return this.messageHandler.remoteSocketAddress();
	}
	
}
