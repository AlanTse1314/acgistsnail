package com.acgist.snail.net;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;

import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>UDP通道</p>
 * 
 * @author acgist
 * @since 1.2.1
 */
public interface UdpChannel {
	
	/**
	 * 随机端口
	 */
	int PORT_AUTO = -1;
	/**
	 * 本机地址
	 */
	String ADDR_LOCAL = null;
	/**
	 * 重用地址
	 */
	boolean ADDR_REUSE = true;
	/**
	 * 不重用地址
	 */
	boolean ADDR_USENEW = false;
	
	/**
	 * <p>创建UDP通道</p>
	 * <p>通道属性：本机地址、随机端口、不重用地址</p>
	 */
	public default DatagramChannel buildUdpChannel() throws NetException {
		return buildUdpChannel(PORT_AUTO, ADDR_LOCAL, ADDR_USENEW);
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * <p>通道属性：本机地址、不重用地址</p>
	 */
	public default DatagramChannel buildUdpChannel(int port) throws NetException {
		return buildUdpChannel(port, ADDR_LOCAL, ADDR_USENEW);
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * <p>通道属性：不重用地址</p>
	 */
	public default DatagramChannel buildUdpChannel(int port, String host) throws NetException {
		return buildUdpChannel(port, host, ADDR_USENEW);
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * <p>通道属性：本机地址</p>
	 */
	public default DatagramChannel buildUdpChannel(int port, boolean reuse) throws NetException {
		return buildUdpChannel(port, ADDR_LOCAL, reuse);
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * 
	 * @param port 端口：{@linkplain #PORT_AUTO 随机端口}
	 * @param host 地址：{@linkplain #ADDR_LOCAL 本机地址}
	 * @param reuse 是否重用地址：{@linkplain #ADDR_REUSE 重用}、{@linkplain #ADDR_USENEW 不重用}
	 * 
	 * @return UDP通道
	 * 
	 * @throws NetException 网络异常
	 */
	public default DatagramChannel buildUdpChannel(int port, String host, boolean reuse) throws NetException {
		boolean ok = true;
		DatagramChannel channel = null;
		try {
//			channel = DatagramChannel.open();
			channel = DatagramChannel.open(StandardProtocolFamily.INET); // IPv4
			channel.configureBlocking(false); // 不阻塞
			if(reuse) {
				channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			}
			if(port >= 0) {
				channel.bind(NetUtils.buildSocketAddress(host, port)); // 绑定：使用receive、send方法
//				channel.connect(NetUtils.buildSocketAddress(host, port)); // 连接：使用read、write方法
			}
		} catch (IOException e) {
			ok = false;
			throw new NetException("创建UDP通道失败", e);
		} finally {
			if(ok) {
				// 成功
			} else {
				IoUtils.close(channel);
				channel = null;
			}
		}
		return channel;
	}
	
}
