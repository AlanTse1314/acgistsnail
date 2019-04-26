package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * UDP客户端
 */
public abstract class UdpClient<T extends UdpMessageHandler> {
	
	public static final String UDP_REGEX = "udp://.*";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpClient.class);

	/**
	 * 客户端名称
	 */
	private final String name;
	private final T handler;
	
	private static final ExecutorService EXECUTOR;
	
	static {
		EXECUTOR = SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_UDP_CLIENT);
	}
	
	private DatagramChannel channel;
	
	public UdpClient(String name, T handler) {
		this.name = name;
		this.handler = handler;
	}

	/**
	 * 打开客户端
	 */
	public boolean open() {
		boolean ok = true;
		try {
			this.channel = DatagramChannel.open(StandardProtocolFamily.INET); // TPv4
			this.channel.configureBlocking(false); // 不阻塞
//			this.channel.connect(new InetSocketAddress(host, port)); // 连接后使用：read、write
		} catch (IOException e) {
			ok = false;
			LOGGER.error("UDP打开端口异常", e);
		}
		if(ok) {
		} else {
			this.close();
		}
		return ok;
	}

	/**
	 * 多播分组
	 */
	public void join(String group) {
		try {
			this.channel.setOption(StandardSocketOptions.IP_MULTICAST_TTL, 2);
			this.channel.join(InetAddress.getByName(group), NetUtils.defaultNetworkInterface());
		} catch (IOException e) {
			LOGGER.info("UDP多播异常：{}", group, e);
		}
	}

	/**
	 * 绑定消息处理器
	 */
	public void handle() {
		EXECUTOR.submit(() -> {
			try {
				this.handler.handle(channel);
			} catch (IOException e) {
				LOGGER.error("UDP消息代理异常", e);
			}
		});
	}
	
	/**
	 * 发送消息
	 */
	public void send(ByteBuffer buffer, SocketAddress address) throws NetException {
		if(!channel.isOpen()) {
			LOGGER.debug("发送消息时Socket已经关闭");
			return;
		}
		if(buffer.position() != 0) { //  重置标记
			buffer.flip();
		}
		if(buffer.limit() == 0) {
			LOGGER.warn("发送消息为空");
			return;
		}
		// 不用保证顺序
		try {
			final int size = channel.send(buffer, address);
			if(size <= 0) {
				LOGGER.warn("发送数据为空");
			}
		} catch (Exception e) {
			throw new NetException(e);
		}
	}

	/**
	 * 关闭channel
	 */
	public void close() {
		LOGGER.debug("UDP Client关闭：{}", this.name);
		IoUtils.close(channel);
	}

	/**
	 * 验证UCP协议
	 */
	public static final boolean verify(String url) {
		return StringUtils.regex(url, UDP_REGEX, true);
	}

	/**
	 * 关闭Client线程池
	 */
	public static final void shutdown() {
		LOGGER.info("关闭UDP Client线程池");
		SystemThreadContext.shutdown(EXECUTOR);
	}
	
}
