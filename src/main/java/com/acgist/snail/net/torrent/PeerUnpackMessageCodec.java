package com.acgist.snail.net.torrent;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.exception.PacketSizeException;

/**
 * <p>Peer消息处理器：拆包</p>
 * 
 * TODO：握手消息匹配协议名称
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class PeerUnpackMessageCodec extends MessageCodec<ByteBuffer, ByteBuffer> {

	/**
	 * <p>int类型数据字符长度</p>
	 */
	private static final int INT_BYTE_LENGTH = 4;
	
	/**
	 * <p>消息缓存</p>
	 * <p>处理消息没有接收完整的情况</p>
	 */
	private ByteBuffer buffer;
	/**
	 * <p>消息长度</p>
	 */
	private final ByteBuffer lengthStick;
	/**
	 * <p>Peer消息代理</p>
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;
	
	public PeerUnpackMessageCodec(PeerSubMessageHandler peerSubMessageHandler) {
		super(peerSubMessageHandler);
		this.lengthStick = ByteBuffer.allocate(INT_BYTE_LENGTH);
		this.peerSubMessageHandler = peerSubMessageHandler;
	}
	
	@Override
	public void decode(ByteBuffer buffer, InetSocketAddress address, boolean haveAddress) throws NetException {
		int length = 0; // 消息数据长度
		while(true) {
			if(this.buffer == null) {
				if(this.peerSubMessageHandler.handshake()) {
					for (int index = 0; index < buffer.limit() && buffer.hasRemaining(); index++) {
						this.lengthStick.put(buffer.get());
						if(this.lengthStick.position() == INT_BYTE_LENGTH) {
							break;
						}
					}
					if(this.lengthStick.position() == INT_BYTE_LENGTH) {
						this.lengthStick.flip();
						length = this.lengthStick.getInt();
						this.lengthStick.compact();
					} else { // 消息长度读取不完整跳出
						break;
					}
				} else { // 握手消息长度
					length = PeerConfig.HANDSHAKE_LENGTH;
				}
				// 心跳消息
				if(length <= 0) {
					this.peerSubMessageHandler.keepAlive();
					if(buffer.hasRemaining()) { // 还有消息：继续处理
						continue;
					} else { // 没有消息：跳出循环
						break;
					}
				}
				PacketSizeException.verify(length);
				this.buffer = ByteBuffer.allocate(length);
			} else {
				// 上次消息没有读取完成：计算剩余消息数据长度
				length = this.buffer.capacity() - this.buffer.position();
			}
			final int remaining = buffer.remaining();
			if(remaining > length) { // 包含一条完整消息：处理完成后继续读取
				final byte[] bytes = new byte[length];
				buffer.get(bytes);
				this.buffer.put(bytes);
				this.doNext(this.buffer, address, haveAddress);
				this.buffer = null;
			} else if(remaining == length) { // 刚好一条完整消息：处理完成后跳出循环
				final byte[] bytes = new byte[length];
				buffer.get(bytes);
				this.buffer.put(bytes);
				this.doNext(this.buffer, address, haveAddress);
				this.buffer = null;
				break;
			} else if(remaining < length) { // 不是一条完整消息：跳出循环等待后续数据
				final byte[] bytes = new byte[remaining];
				buffer.get(bytes);
				this.buffer.put(bytes);
				break;
			}
		}
	}

}
