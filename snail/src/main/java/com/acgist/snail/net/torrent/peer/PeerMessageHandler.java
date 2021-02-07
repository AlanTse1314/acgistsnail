package com.acgist.snail.net.torrent.peer;

import java.nio.ByteBuffer;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.codec.IMessageEncoder;
import com.acgist.snail.net.torrent.IMessageEncryptSender;
import com.acgist.snail.net.torrent.IPeerConnect;

/**
 * <p>Peer消息代理</p>
 * 
 * @author acgist
 */
public final class PeerMessageHandler extends TcpMessageHandler implements IMessageEncryptSender {

	/**
	 * <p>是否已经检查</p>
	 */
	private boolean uselessCheck = false;
	/**
	 * <p>消息编码器</p>
	 */
	private final IMessageEncoder<ByteBuffer> messageEncoder;
	/**
	 * <p>Peer消息代理</p>
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;
	
	/**
	 * <p>服务端</p>
	 */
	public PeerMessageHandler() {
		this(PeerSubMessageHandler.newInstance());
	}

	/**
	 * <p>客户端</p>
	 * 
	 * @param peerSubMessageHandler Peer消息代理
	 */
	public PeerMessageHandler(PeerSubMessageHandler peerSubMessageHandler) {
		peerSubMessageHandler.messageEncryptSender(this);
		final var peerUnpackMessageCodec = new PeerUnpackMessageCodec(peerSubMessageHandler);
		final var peerCryptMessageCodec = new PeerCryptMessageCodec(peerUnpackMessageCodec, peerSubMessageHandler);
		this.messageDecoder = peerCryptMessageCodec;
		this.messageEncoder = peerCryptMessageCodec;
		this.peerSubMessageHandler = peerSubMessageHandler;
	}
	
	@Override
	public void sendEncrypt(ByteBuffer buffer, int timeout) throws NetException {
		this.messageEncoder.encode(buffer);
		this.send(buffer, timeout);
	}
	
	@Override
	public IPeerConnect.ConnectType connectType() {
		return IMessageEncryptSender.ConnectType.TCP;
	}
	
	@Override
	public boolean useless() {
		final boolean handshake = this.peerSubMessageHandler.handshakeRecv();
		if(handshake) {
			return false;
		}
		if(this.uselessCheck) {
			return true;
		}
		this.uselessCheck = true;
		return false;
	}

}
