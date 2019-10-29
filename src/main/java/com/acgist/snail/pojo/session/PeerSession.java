package com.acgist.snail.pojo.session;

import java.net.InetSocketAddress;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import com.acgist.snail.net.torrent.bootstrap.PeerConnect;
import com.acgist.snail.net.torrent.bootstrap.PeerLauncher;
import com.acgist.snail.system.IStatistics;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Peer Session</p>
 * <p>保存Peer信息：ip、端口、下载统计等。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class PeerSession implements IStatistics {
	
	/**
	 * PeerID
	 */
	private byte[] id;
	/**
	 * Peer客户端名称
	 */
	private String clientName;
	/**
	 * 失败次数
	 */
	private volatile int failTimes = 0;
	/**
	 * 保留位
	 */
	private volatile byte[] reserved;
	/**
	 * pex flags
	 */
	private volatile byte flags = 0;
	/**
	 * 状态：需要同步
	 */
	private volatile byte status = 0;
	/**
	 * 来源属性
	 */
	private volatile byte source = 0;
	/**
	 * Peer地址
	 */
	private String host;
	/**
	 * Peer端口
	 */
	private Integer port;
	/**
	 * DHT端口
	 */
	private Integer dhtPort;
	/**
	 * 文件下载位图
	 */
	private final BitSet pieces;
	/**
	 * 下载错误位图：下次获取时排除
	 */
	private final BitSet badPieces;
	/**
	 * 客户端将Peer阻塞：阻塞-1（true）、非阻塞-0
	 */
	private volatile boolean amChocking;
	/**
	 * 客户端对Peer感兴趣：感兴趣-1（true）、不感兴趣-0
	 */
	private volatile boolean amInterested;
	/**
	 * Peer将客户阻塞：阻塞-1（true）、非阻塞-0
	 */
	private volatile boolean peerChocking;
	/**
	 * Peer对客户端感兴趣：感兴趣-1、不感兴趣-0
	 */
	private volatile boolean peerInterested;
	/**
	 * 接入Peer（上传）
	 */
	private PeerConnect peerConnect;
	/**
	 * 连接Peer（下载）
	 */
	private PeerLauncher peerLauncher;
	/**
	 * 统计信息
	 */
	private final StatisticsSession statistics;
	/**
	 * 支持的扩展协议：协议=协议ID
	 */
	private final Map<PeerConfig.ExtensionType, Byte> extension;

	private PeerSession(StatisticsSession parent, String host, Integer port) {
		this.host = host;
		this.port = port;
		this.amChocking = true;
		this.amInterested = false;
		this.peerChocking = true;
		this.peerInterested = false;
		this.pieces = new BitSet();
		this.badPieces = new BitSet();
		this.extension = new HashMap<>();
		this.statistics = new StatisticsSession(parent);
	}
	
	public static final PeerSession newInstance(StatisticsSession parent, String host, Integer port) {
		return new PeerSession(parent, host, port);
	}
	
	public void amChoke() {
		this.amChocking = true;
	}
	
	public void amUnchoke() {
		this.amChocking = false;
	}
	
	public void amInterested() {
		this.amInterested = true;
	}
	
	public void amNotInterested() {
		this.amInterested = false;
	}
	
	public void peerChoke() {
		this.peerChocking = true;
	}
	
	public void peerUnchoke() {
		this.peerChocking = false;
	}
	
	public void peerInterested() {
		this.peerInterested = true;
	}
	
	public void peerNotInterested() {
		this.peerInterested = false;
	}
	public boolean isAmChocking() {
		return this.amChocking;
	}
	
	public boolean isAmInterested() {
		return this.amInterested;
	}
	
	public boolean isPeerChocking() {
		return this.peerChocking;
	}
	
	public boolean isPeerInterested() {
		return this.peerInterested;
	}
	
	/**
	 * 判断是否存在：判断IP不判断端口
	 */
	public boolean equals(String host) {
		return StringUtils.equals(this.host, host);
	}

	@Override
	public void download(long buffer) {
		this.statistics.download(buffer);
	}
	
	@Override
	public void upload(long buffer) {
		this.statistics.upload(buffer);
	}
	
	public StatisticsSession statistics() {
		return this.statistics;
	}
		
	/**
	 * 可以上传：Peer对客户端感兴趣并且客户端未阻塞Peer
	 */
	public boolean uploadable() {
		return this.peerInterested && !this.amChocking;
	}
	
	/**
	 * 可以下载：客户端对Peer感兴趣并且Peer未阻塞客户端
	 */
	public boolean downloadable() {
		return this.amInterested && !this.peerChocking;
	}
	
	/**
	 * 设置ID和客户端名称
	 */
	public void id(byte[] id) {
		this.id = id;
		this.clientName = PeerConfig.name(this.id);
	}
	
	public String clientName() {
		return this.clientName;
	}
	
	public String host() {
		return this.host;
	}
	
	public Integer port() {
		return this.port;
	}
	
	public void port(Integer port) {
		this.port = port;
	}
	
	public BitSet pieces() {
		return this.pieces;
	}
	
	/**
	 * 设置Peer位图
	 * 
	 * @param pieces Peer位图
	 */
	public void pieces(BitSet pieces) {
		this.pieces.or(pieces);
	}

	/**
	 * 设置Peer位图
	 * 
	 * @param index Piece序号
	 */
	public void piece(int index) {
		this.pieces.set(index);
	}
	
	/**
	 * 是否含有Piece
	 */
	public boolean havePiece(int index) {
		if(index < 0) {
			return false;
		}
		return this.pieces.get(index);
	}
	
	/**
	 * 设置无效Piece（校验失败的Piece）
	 */
	public void badPieces(int index) {
		this.badPieces.set(index);
	}
	
	/**
	 * <p>获取可用的Piece位图</p>
	 * <p>Peer已有Piece去除无效Piece位图</p>
	 */
	public BitSet availablePieces() {
		final BitSet bitSet = new BitSet();
		bitSet.or(this.pieces);
		bitSet.andNot(this.badPieces);
		return bitSet;
	}
	
	/**
	 * 添加扩展类型
	 */
	public void addExtensionType(PeerConfig.ExtensionType type, byte typeId) {
		this.extension.put(type, typeId);
	}
	
	/**
	 * 是否支持扩展协议
	 */
	public boolean supportExtensionType(PeerConfig.ExtensionType type) {
		return this.extension.containsKey(type);
	}

	/**
	 * 获取扩展协议ID
	 */
	public Byte extensionTypeId(PeerConfig.ExtensionType type) {
		return this.extension.get(type);
	}
	
	/**
	 * 增加失败次数
	 */
	public void fail() {
		this.failTimes++;
	}
	
	/**
	 * <dl>
	 * 	<dt>是否可用：</dt>
	 * 	<dd>失败次数小于最大失败次数</dd>
	 * 	<dd>有可用端口（主动连接的客户端可能没有端口号）</dd>
	 * </dl>
	 */
	public boolean available() {
		return
			this.failTimes < PeerConfig.MAX_FAIL_TIMES &&
			this.port != null;
	}
	
	public Integer dhtPort() {
		return this.dhtPort;
	}
	
	public void dhtPort(Integer dhtPort) {
		this.dhtPort = dhtPort;
	}
	
	public void reserved(byte[] reserved) {
		this.reserved = reserved;
	}
	
	/**
	 * 是否支持扩展协议
	 */
	public boolean supportExtensionProtocol() {
		return this.reserved != null && (this.reserved[5] & PeerConfig.EXTENSION_PROTOCOL) != 0;
	}

	/**
	 * 是否支持DHT扩展协议
	 */
	public boolean supportDhtProtocol() {
		return this.reserved != null && (this.reserved[7] & PeerConfig.DHT_PROTOCOL) != 0;
	}

	/**
	 * 设置来源
	 */
	public void source(byte source) {
		this.source |= source;
	}

	/**
	 * 验证来源
	 */
	public boolean verifySource(byte source) {
		return (this.source & source) != 0;
	}
	
	/**
	 * 来源：DHT
	 */
	public boolean dht() {
		return verifySource(PeerConfig.SOURCE_DHT);
	}
	
	/**
	 * 来源：LSD
	 */
	public boolean lsd() {
		return verifySource(PeerConfig.SOURCE_LSD);
	}
	
	/**
	 * 来源：PEX
	 */
	public boolean pex() {
		return verifySource(PeerConfig.SOURCE_PEX);
	}

	/**
	 * 来源：Tracker
	 */
	public boolean tracker() {
		return verifySource(PeerConfig.SOURCE_TRACKER);
	}
	
	/**
	 * 来源：连接
	 */
	public boolean connect() {
		return verifySource(PeerConfig.SOURCE_CONNECT);
	}
	
	/**
	 * 来源：HOLEPUNCH
	 */
	public boolean holepunch() {
		return verifySource(PeerConfig.SOURCE_HOLEPUNCH);
	}
	
	/**
	 * 设置状态
	 */
	public void status(byte status) {
		synchronized (this) {
			this.status |= status;
		}
	}
	
	/**
	 * 取消状态
	 */
	public void statusOff(byte status) {
		synchronized (this) {
			this.status ^= status;
		}
	}
	
	/**
	 * 验证状态
	 */
	private boolean verifyStatus(byte status) {
		return (this.status & status) != 0;
	}
	
	/**
	 * 是否上传中
	 */
	public boolean uploading() {
		return verifyStatus(PeerConfig.STATUS_UPLOAD);
	}
	
	/**
	 * 是否下载中
	 */
	public boolean downloading() {
		return verifyStatus(PeerConfig.STATUS_DOWNLOAD);
	}
	
	/**
	 * 连接中：上传中或者下载中
	 */
	public boolean connected() {
		return uploading() || downloading();
	}
	
	/**
	 * 获取pex flags
	 */
	public byte flags() {
		return this.flags;
	}
	
	/**
	 * 设置pex flags
	 */
	public void flags(byte flags) {
		this.flags |= flags;
	}
	
	/**
	 * 取消pex flags
	 */
	public void flagsOff(byte flags) {
		this.flags ^= flags;
	}
	
	/**
	 * 验证pex flags
	 */
	private boolean verifyFlags(byte flags) {
		return (this.flags & flags) != 0;
	}
	
	/**
	 * 是否支持UTP
	 */
	public boolean utp() {
		return verifyFlags(PeerConfig.PEX_UTP);
	}

	/**
	 * 是否偏爱加密
	 */
	public boolean encrypt() {
		return verifyFlags(PeerConfig.PEX_PREFER_ENCRYPTION);
	}
	
	/**
	 * 是否只上传不下载
	 */
	public boolean uploadOnly() {
		return verifyFlags(PeerConfig.PEX_UPLOAD_ONLY);
	}
	
	public PeerConnect peerConnect() {
		return this.peerConnect;
	}
	
	public void peerConnect(PeerConnect peerConnect) {
		this.peerConnect = peerConnect;
	}
	
	public PeerLauncher peerLauncher() {
		return this.peerLauncher;
	}
	
	public void peerLauncher(PeerLauncher peerLauncher) {
		this.peerLauncher = peerLauncher;
	}
	
	public InetSocketAddress peerSocketAddress() {
		return NetUtils.buildSocketAddress(this.host, this.port);
	}
	
	public InetSocketAddress dhtSocketAddress() {
		return NetUtils.buildSocketAddress(this.host, this.dhtPort);
	}
	
	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(this.host);
	}
	
	@Override
	public boolean equals(Object object) {
		if(ObjectUtils.equals(this, object)) {
			return true;
		}
		if(object instanceof PeerSession) {
			final PeerSession peerSession = (PeerSession) object;
			return StringUtils.equals(this.host, peerSession.host);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.host, this.port, this.dhtPort);
	}

}
