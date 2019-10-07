package com.acgist.snail.net.torrent.lsd;

import com.acgist.snail.net.UdpServer;
import com.acgist.snail.utils.NetUtils;

/**
 * 本地发现服务端
 * 
 * @author acgist
 * @since 1.0.0
 */
public class LocalServiceDiscoveryServer extends UdpServer<LocalServiceDiscoveryAcceptHandler> {
	
	private static final LocalServiceDiscoveryServer INSTANCE = new LocalServiceDiscoveryServer();

	/**
	 * TTL
	 */
	private static final int LSD_TTL = 2;
	/**
	 * 本地发现端口
	 */
	public static final int LSD_PORT = 6771;
	/**
	 * 本地发现IPv4地址
	 */
	public static final String LSD_HOST = "239.192.152.143";
	/**
	 * 本地发现IPv6地址
	 */
	public static final String LSD_HOST_IPV6 = "[ff15::efc0:988f]";
	
	public LocalServiceDiscoveryServer() {
		super(NetUtils.buildUdpChannel(LSD_PORT, true), "LSD Server", LocalServiceDiscoveryAcceptHandler.getInstance());
		this.join(LSD_TTL, LSD_HOST);
		this.handle();
	}
	
	public static final LocalServiceDiscoveryServer getInstance() {
		return INSTANCE;
	}

}
