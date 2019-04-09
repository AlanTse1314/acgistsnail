package com.acgist.snail.net.peer;

import java.nio.ByteBuffer;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;

/**
 * Peer消息处理
 */
public class PeerMessageHandler extends TcpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerMessageHandler.class);
	
	/**
	 * 协议名称
	 */
	public static final byte[] HANDSHAKE_NAME = "BitTorrent protocol".getBytes();
	private static final byte[] HANDSHAKE_RESERVED = {0, 0, 0, 0, 0, 0, 0, 0};
	private static final int HANDSHAKE_LENGTH = 68;
	
	private volatile boolean handshake = false; // 是否握手
	private ByteBuffer buffer;
	
	private final PeerSession peerSession;
	private final TorrentSession torrentSession;
	
	public PeerMessageHandler(PeerSession peerSession, TorrentSession torrentSession) {
		super("");
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
	}

	@Override
	public boolean doMessage(Integer result, ByteBuffer attachment) {
		boolean doNext = true; // 是否继续处理消息
		if (result == 0) {
			LOGGER.info("读取空消息");
		} else {
			while(true) {
				int length = 0;
				final int position = attachment.position();
				attachment.flip();
				if(buffer == null) {
					if(handshake) {
						length = attachment.getInt();
					} else { // 握手
						length = HANDSHAKE_LENGTH;
					}
					if(length == 0) {
						break;
					}
					buffer = ByteBuffer.allocate(length);
				} else {
					length = buffer.capacity() - buffer.position();
				}
				if(position > length) { // 包含一个完整消息
					byte[] bytes = new byte[length];
					attachment.get(bytes);
					buffer.put(bytes);
					oneMessage(buffer);
					buffer = null;
				} else if(position == length) { // 刚好一个完整消息
					byte[] bytes = new byte[length];
					attachment.get(bytes);
					buffer.put(bytes);
					oneMessage(buffer);
					buffer = null;
					break;
				} else if(position < length) { // 不是完整消息
					byte[] bytes = new byte[position];
					attachment.get(bytes);
					buffer.put(bytes);
					break;
				}
			}
		}
		return doNext;
	}
	
	private boolean oneMessage(final ByteBuffer buffer) {
		buffer.flip();
		if(!handshake) {
			handshake = true;
			handshake(buffer);
		}
		return true;
	}

	/**
	 * 握手：
	 * 消息格式：pstrlen pstr reserved info_hash peer_id
	 * pstrlen：pstr的长度：19<br>
	 * pstr：BitTorrent协议的关键字：BitTorrent protocol<br>
	 * reserved：8字节，用于扩展BT协议，一般都设置：0<br>
	 * info_hash：info_hash<br>
	 * peer_id：peer_id
	 */
	public void handshake() {
		ByteBuffer buffer = ByteBuffer.allocate(HANDSHAKE_LENGTH);
		buffer.put((byte) HANDSHAKE_NAME.length);
		buffer.put(HANDSHAKE_NAME);
		buffer.put(HANDSHAKE_RESERVED);
		buffer.put(torrentSession.infoHash().hash());
		buffer.put(PeerServer.PEER_ID.getBytes());
		send(buffer);
	}
	
	/**
	 * 处理握手
	 */
	private void handshake(ByteBuffer buffer) {
		final byte[] peerIds = new byte[20];
		buffer.position(48);
		buffer.get(peerIds);
		peerSession.id(new String(peerIds));
	}
	
	/**
	 * 4字节：消息持久：len=0000
	 * 只有消息长度，没有消息编号和负载
	 */
	public void keepAlive() {
		send(buildMessage(null, null));
	}
	
	/**
	 * 5字节：len=0001 id=0
	 * 阻塞
	 */
	public void choke() {
		send(buildMessage(Byte.decode("0"), null));
	}
	
	/**
	 * 5字节：len=0001 id=1
	 * 解除阻塞
	 */
	public void unchoke() {
		send(buildMessage(Byte.decode("1"), null));
	}
	
	/**
	 * 5字节：len=0001 id=2
	 * 收到have消息时，客户端对Peer感兴趣
	 */
	public void interested() {
		send(buildMessage(Byte.decode("2"), null));
	}
	
	/**
	 * 5字节：len=0001 id=3
	 * 客户端对Peer不感兴趣
	 */
	public void notInterested() {
		send(buildMessage(Byte.decode("3"), null));
	}
	
	/**
	 * 5字节：len=0005 id=4 piece_index
	 * piece index：piece下标，每当客户端下载完piece，发出have消息告诉所有与客户端连接的Peer
	 */
	public void have(int index) {
		send(buildMessage(Byte.decode("4"), ByteBuffer.allocate(4).putInt(index).array()));
	}
	
	/**
	 * 长度不固定：len=0001+X id=5 bitfield
	 * 交换位图：x=bitfield.length，握手后交换位图，每个piece占一位
	 */
	public void bitfield(BitSet pieces) {
		send(buildMessage(Byte.decode("5"), pieces.toByteArray()));
	}
	
	/**
	 * 13字节：len=0013 id=6 index begin length
	 * index：piece的索引
	 * begin：piece内的偏移
	 * length：请求Peer发送的数据的长度
	 * 当客户端收到Peer的unchoke请求后即可构建request消息，一般交换数据是以slice（长度16KB的块）为单位的
	 */
	public void request(int index, int begin, int length) {
		ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.putInt(length);
		send(buildMessage(Byte.decode("6"), buffer.array()));
	}
	
	/**
	 * 长度不固定：len=0009+X id=7 index begin block
	 * piece消息：x=block长度（一般为16KB），收到request消息，如果没有Peer未被阻塞，且存在slice，则返回数据
	 */
	public void piece() {
		send(buildMessage(Byte.decode("7"), null));
	}
	
	/**
	 * 13字节：len=0013 id=8 index begin length
	 * 与request作用相反，取消下载
	 */
	public void cancel(int index, int begin, int length) {
		ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.putInt(length);
		send(buildMessage(Byte.decode("8"), buffer.array()));
	}
	
	/**
	 * 3字节：len=0003 id=9 listen-port
	 * listen-port：两字节
	 * 支持DHT的客户端使用，指明DHT监听的端口
	 */
	public void port(short port) {
		send(buildMessage(Byte.decode("9"), ByteBuffer.allocate(4).putShort(port).array()));
	}
	
	/**
	 * 消息：
	 * 消息格式：length_prefix message_ID payload<br>
	 * length prefix：4字节：message id和payload的长度和<br>
	 * message id：1字节：指明消息的编号<br>
	 * payload：消息内容
	 */
	public ByteBuffer buildMessage(Byte id, byte[] payload) {
		int capacity = 0;
		if(id != null) {
			capacity += 1;
		}
		if(payload != null) {
			capacity += payload.length;
		}
		ByteBuffer buffer = ByteBuffer.allocate(capacity + 4); // +4=length prefix
		buffer.putInt(capacity);
		if(id != null) {
			buffer.put(id);
		}
		if(payload != null) {
			buffer.put(payload);
		}
		return buffer;
	}

}
