package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.system.exception.NetException;

/**
 * 消息代理
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class MessageHandlerClientAdapter<T extends IMessageHandler> implements IMessageHandler {

	/**
	 * 消息代理
	 */
	protected T handler;
	
	@Override
	public void close() {
		if(this.handler != null) {
			this.handler.close();
		}
	}

	@Override
	public boolean available() {
		if(this.handler == null) {
			return false;
		} else {
			return this.handler.available();
		}
	}

	@Override
	public void send(String message) throws NetException {
		this.handler.send(message);
	}

	@Override
	public void send(byte[] bytes) throws NetException {
		this.handler.send(bytes);
	}

	@Override
	public void send(ByteBuffer buffer) throws NetException {
		this.handler.send(buffer);
	}

	@Override
	public InetSocketAddress remoteSocketAddress() {
		return this.handler.remoteSocketAddress();
	}

}
