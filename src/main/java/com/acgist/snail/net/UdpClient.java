package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.NetUtils;

/**
 * <p>UDP客户端</p>
 * <p>UDP客户端、服务端通道都是公用一个：</p>
 * <ul>
 * 	<li>单例</li>
 * 	<li>UDP通道使用服务器通道</li>
 * </ul>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class UdpClient<T extends UdpMessageHandler> extends ClientMessageHandlerAdapter<T> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpClient.class);

	/**
	 * 客户端名称
	 */
	private final String name;
	/**
	 * 远程地址
	 */
	protected final InetSocketAddress socketAddress;
	
	/**
	 * 新建客户端
	 * 
	 * @param name 客户端名称
	 * @param handler 消息处理器
	 * @param socketAddress 远程地址
	 */
	public UdpClient(String name, T handler, InetSocketAddress socketAddress) {
		super(handler);
		this.name = name;
		this.socketAddress = socketAddress;
		this.open();
	}

	/**
	 * <p>打开客户端</p>
	 * <p>随机端口</p>
	 * 
	 * @return 打开状态
	 */
	public abstract boolean open();
	
	/**
	 * 打开客户端
	 * 
	 * @param port 端口
	 * 
	 * @return 打开状态
	 */
	public boolean open(final int port) {
		return this.open(null, port);
	}

	/**
	 * 打开客户端
	 * 
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @return 打开状态
	 */
	public boolean open(final String host, final int port) {
		final DatagramChannel channel = NetUtils.buildUdpChannel(host, port);
		return open(channel);
	}
	
	/**
	 * <p>打开客户端</p>
	 * <p>客户端和服务端的使用同一个通道</p>
	 * 
	 * @param channel 通道
	 * 
	 * @return 打开状态
	 * 
	 */
	public boolean open(DatagramChannel channel) {
		if(channel == null) {
			return false;
		}
		this.handler.handle(channel, this.socketAddress);
		return true;
	}
	
	/**
	 * <p>关闭资源，标记关闭，不能关闭通道。</p>
	 * <p>UDP通道只打开一个，程序结束时才能关闭。</p>
	 */
	@Override
	public void close() {
		LOGGER.debug("关闭UDP Client：{}", this.name);
		super.close();
	}

}
