package com.acgist.snail.net.torrent;

import java.nio.ByteBuffer;

import com.acgist.snail.net.crypt.MSECryptHanlder;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.system.config.CryptConfig;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Peer消息处理：加密、解密</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public class PeerCryptMessageHandler {

	/**
	 * MSE加密处理器
	 */
	private final MSECryptHanlder mseCryptHanlder;
	
	private final PeerUnpackMessageHandler peerUnpackMessageHandler;
	
	private PeerCryptMessageHandler(PeerSubMessageHandler peerSubMessageHandler) {
		this.peerUnpackMessageHandler = PeerUnpackMessageHandler.newInstance(peerSubMessageHandler);
		this.mseCryptHanlder = MSECryptHanlder.newInstance(peerSubMessageHandler, this.peerUnpackMessageHandler);
	}
	
	public static final PeerCryptMessageHandler newInstance(PeerSubMessageHandler peerSubMessageHandler) {
		return new PeerCryptMessageHandler(peerSubMessageHandler);
	}
	
	/**
	 * 处理消息
	 */
	public void onMessage(ByteBuffer buffer) throws NetException {
		if(this.mseCryptHanlder.over()) { // 握手完成
			this.mseCryptHanlder.decrypt(buffer);
			this.peerUnpackMessageHandler.onMessage(buffer);
		} else { // 握手
			this.mseCryptHanlder.handshake(buffer);
		}
	}
	
	/**
	 * 消息加密
	 */
	public void encrypt(ByteBuffer buffer) {
		if(this.mseCryptHanlder.over()) { // 握手完成
			this.mseCryptHanlder.encrypt(buffer); // 加密消息
		} else {
			if(CryptConfig.STRATEGY.crypt()) { // 加密
				this.mseCryptHanlder.handshake(); // 握手
				this.mseCryptHanlder.handshakeLock(); // 加锁
				this.mseCryptHanlder.encrypt(buffer); // 加密消息
			} else { // 明文
				this.mseCryptHanlder.plaintext();
			}
		}
	}
	
}
