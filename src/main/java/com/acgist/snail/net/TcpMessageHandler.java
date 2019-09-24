package com.acgist.snail.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.IoUtils;

/**
 * TCP消息
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class TcpMessageHandler implements CompletionHandler<Integer, ByteBuffer>, IMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpMessageHandler.class);
	
	public static final int BUFFER_SIZE = 10 * 1024;
	
	/**
	 * 消息分隔符
	 */
	private final String split;
	/**
	 * 是否关闭
	 */
	private boolean close = false;
	/**
	 * Socket
	 */
	protected AsynchronousSocketChannel socket;
	
	public TcpMessageHandler() {
		this(null);
	}

	public TcpMessageHandler(String split) {
		this.split = split;
	}
	
	/**
	 * 收到消息
	 */
	public abstract void onReceive(ByteBuffer buffer) throws NetException;

	/**
	 * 消息代理
	 */
	public void handle(AsynchronousSocketChannel socket) {
		this.socket = socket;
		loopMessage();
	}
	
	@Override
	public boolean available() {
		return !this.close && this.socket != null;
	}
	
	@Override
	public void send(String message) throws NetException {
		send(message, null);
	}
	
	@Override
	public void send(String message, String charset) throws NetException {
		String splitMessage = message;
		if(this.split != null) {
			splitMessage += this.split;
		}
		if(charset == null) {
			send(splitMessage.getBytes());
		} else {
			try {
				send(splitMessage.getBytes(charset));
			} catch (UnsupportedEncodingException e) {
				throw new NetException(String.format("编码异常，编码：%s，内容：%s。", charset, message), e);
			}
		}
	}
	
	@Override
	public void send(byte[] bytes) throws NetException {
		send(ByteBuffer.wrap(bytes));
	}
	
	@Override
	public void send(ByteBuffer buffer) throws NetException {
		if(!available()) {
			LOGGER.debug("发送消息时Socket已经不可用");
			return;
		}
		if(buffer.position() != 0) { //  重置标记
			buffer.flip();
		}
		if(buffer.limit() == 0) {
			LOGGER.warn("发送消息为空");
			return;
		}
		synchronized (this.socket) { // 防止多线程同时读写导致WritePendingException
			final Future<Integer> future = this.socket.write(buffer);
			try {
				final int size = future.get(4, TimeUnit.SECONDS); // 阻塞线程防止，防止多线程写入时抛出异常：IllegalMonitorStateException
				if(size <= 0) {
					LOGGER.warn("发送数据为空");
				}
			} catch (Exception e) {
				throw new NetException(e);
			}
		}
	}

	@Override
	public InetSocketAddress remoteSocketAddress() {
		try {
			return (InetSocketAddress) this.socket.getRemoteAddress();
		} catch (IOException e) {
			LOGGER.error("TCP远程客户端信息获取异常", e);
		}
		return null;
	}
	
	@Override
	public void close() {
		this.close = true;
		IoUtils.close(this.socket);
	}
	
	@Override
	public void completed(Integer result, ByteBuffer buffer) {
		if (result == null) {
			this.close();
		} else if(result == -1) { // 服务端关闭
			this.close();
		} else if(result == 0) { // 未遇到过这个情况
			LOGGER.debug("消息长度为零");
		} else {
			try {
				onReceive(buffer);
			} catch (Exception e) {
				LOGGER.error("TCP消息处理异常", e);
			}
		}
		if(available()) {
			loopMessage();
		} else {
			LOGGER.debug("TCP消息代理跳出循环：{}", result);
		}
	}
	
	@Override
	public void failed(Throwable ex, ByteBuffer buffer) {
		LOGGER.error("消息处理异常", ex);
	}
	
	/**
	 * 循环读
	 */
	private void loopMessage() {
		if(available()) {
			final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
			synchronized (this.socket) { // 防止多线程同时读写导致WritePendingException
				this.socket.read(buffer, buffer, this);
			}
		}
	}

}
