package com.acgist.snail.net.handler.message;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.handler.socket.WriterHandler;
import com.acgist.snail.pojo.message.ClientMessage;

/**
 * 抽象消息
 */
public abstract class MessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);
	
	private Lock lock = new ReentrantLock(); // 线程锁
	private Semaphore semaphore = new Semaphore(1); // 信号量
	
	protected AsynchronousSocketChannel socket;
	
	/**
	 * 发送消息
	 */
	protected void send(ClientMessage message) {
		ByteBuffer buffer = ByteBuffer.wrap(message.toBytes());
		this.lock.lock();
		try {
			semaphore.acquire(); // 每次发送一条消息，防止异常：IllegalMonitorStateException
			WriterHandler handler = new WriterHandler(semaphore);
			socket.write(buffer, buffer, handler);
		} catch (Exception e) {
			LOGGER.error("消息发送异常", e);
		} finally {
			this.lock.unlock();
		}
	}

}
