package com.acgist.snail.net.upnp;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NetUtils;

/**
 * UPNP客户
 * 随机端口
 */
public class UpnpClient extends UdpClient<UpnpMessageHandler> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpClient.class);

	// UPNP端口和地址
	private static final int UPNP_PORT = 1900;
	private static final String UPNP_HOST = "239.255.255.250";
	
	private static final UpnpClient INSTANCE = new UpnpClient();
	
	static {
		UdpClient.bindServerHandler(new UpnpMessageHandler(), INSTANCE.channel);
	}
	
	private UpnpClient() {
		super("UPNP Client", new UpnpMessageHandler());
		open();
		join(UPNP_HOST);
	}
	
	public static final UpnpClient getInstance() {
		return INSTANCE;
	}

	/**
	 * 配置UPNP
	 */
	public void config() {
		LOGGER.info("配置UPNP");
		try {
			send(mSearch(), NetUtils.buildSocketAddress(UPNP_HOST, UPNP_PORT));
		} catch (NetException e) {
			LOGGER.error("发送UPNP消息异常", e);
		}
	}
	
	/**
	 * M-SEARCH
	 */
	private ByteBuffer mSearch() {
		StringBuilder builder = new StringBuilder();
		builder.append("M-SEARCH * HTTP/1.1").append("\r\n");
		builder.append("HOST: 239.255.255.250:1900").append("\r\n");
		builder.append("MX: 2").append("\r\n");
		builder.append("ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1").append("\r\n");
		builder.append("MAN: \"ssdp:discover\"").append("\r\n");
		builder.append("\r\n");
		return ByteBuffer.wrap(builder.toString().getBytes());
	}

}
