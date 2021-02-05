package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>UDP客户端</p>
 * <p>UDP客户端通道使用UDP服务端通道</p>
 * 
 * @param <T> UDP消息代理类型
 * 
 * @author acgist
 * 
 * @see UdpServer#channel()
 */
public abstract class UdpClient<T extends UdpMessageHandler> extends ClientMessageHandlerAdapter<T> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpClient.class);

	/**
	 * <p>客户端名称</p>
	 */
	private final String name;
	/**
	 * <p>远程地址</p>
	 */
	protected final InetSocketAddress socketAddress;
	
	/**
	 * <p>创建客户端时自动打开通道</p>
	 * 
	 * @param name 客户端名称
	 * @param handler 消息代理
	 * @param socketAddress 远程地址
	 */
	public UdpClient(String name, T handler, InetSocketAddress socketAddress) {
		super(handler);
		this.name = name;
		this.socketAddress = socketAddress;
		this.open();
	}

	/**
	 * <p>打开通道</p>
	 * 
	 * @return 打开状态
	 */
	public abstract boolean open();
	
	/**
	 * <p>打开通道</p>
	 * 
	 * @param channel 通道
	 * 
	 * @return 打开状态
	 */
	public boolean open(DatagramChannel channel) {
		if(channel == null) {
			return false;
		}
		this.handler.handle(channel, this.socketAddress);
		return true;
	}
	
	@Override
	public void close() {
		LOGGER.debug("关闭UDP Client：{}", this.name);
		super.close();
	}

}
