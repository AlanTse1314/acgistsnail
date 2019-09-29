package com.acgist.snail.net.upnp.bootstrap;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.system.config.ProtocolConfig.Protocol;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.XMLUtils;

/**
 * <p>UPNP Service</p>
 * <p>Internet Gateway Device</p>
 * <p>协议参考：https://tools.ietf.org/html/rfc6970</p>
 * <p>端口映射，将内网的端口映射到外网中。如果外网端口已经被映射，需要设置新的映射端口。</p>
 * 
 * TODO：多路由环境配置
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UpnpService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpService.class);
	
	/**
	 * 映射状态
	 */
	public enum Status {
		
		/** 没有初始化 */
		uninit,
		/** 不可用（已被注册） */
		disable,
		/** 可用（需要注册） */
		mapable,
		/** 可用（已被注册） */
		useable;
		
	}
	
	/**
	 * 控制类型，最后一位类型忽略。
	 */
	private static final String SERVICE_WANIPC = "urn:schemas-upnp-org:service:WANIPConnection:";
	
	/**
	 * 描述文件地址
	 */
	private String location;
	/**
	 * 控制URL
	 */
	private String controlURL;
	/**
	 * 服务类型
	 */
	private String serviceType;
	/**
	 * 可用状态
	 */
	private volatile boolean available = false;
	/**
	 * 外网IP
	 */
	private String externalIpAddress;

	private static final UpnpService INSTANCE = new UpnpService();
	
	private UpnpService() {
	}

	public static final UpnpService getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 加载信息
	 */
	public UpnpService load(String location) throws NetException {
		LOGGER.info("设置UPNP，地址：{}", location);
		this.location = location;
		var response = HTTPClient.get(this.location, BodyHandlers.ofString());
		final String body = response.body();
		final XMLUtils xml = XMLUtils.load(body);
		final List<String> serviceTypes = xml.elementValues("serviceType");
		final List<String> controlURLs = xml.elementValues("controlURL");
		if(CollectionUtils.isEmpty(serviceTypes)) {
			LOGGER.warn("加载UPNP信息失败");
			return this;
		}
		for (int index = 0; index < serviceTypes.size(); index++) {
			String serviceType = serviceTypes.get(index);
			if(StringUtils.startsWith(serviceType, SERVICE_WANIPC)) {
				this.serviceType = serviceType;
				this.controlURL = controlURLs.get(index);
				this.controlURL();
				LOGGER.info("服务类型：{}", this.serviceType);
				LOGGER.info("控制地址：{}", this.controlURL);
				break;
			}
		}
		this.available = true;
		return this;
	}

	/**
	 * 外网IP地址
	 */
	public String externalIpAddress() {
		return this.externalIpAddress;
	}
	
	/**
	 * <p>获取外网IP：GetExternalIPAddress</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#GetExternalIPAddress"</p>
	 */
	public String getExternalIPAddress() throws NetException {
		if(!this.available) {
			return null;
		}
		UpnpRequest upnpRequest = UpnpRequest.newRequest(this.serviceType);
		String xml = upnpRequest.buildGetExternalIPAddress();
		var client = HTTPClient.newInstance(this.controlURL);
		var response = client
			.header("SOAPAction", "\"" + this.serviceType + "#GetExternalIPAddress\"")
			.post(xml, BodyHandlers.ofString());
		String body = response.body();
		return UpnpResponse.parseGetExternalIPAddress(body);
	}

	/**
	 * <p>获取端口映射情况：GetSpecificPortMappingEntry</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#GetSpecificPortMappingEntry"</p>
	 * <p>如果没有映射：返回{@link HTTPClient#HTTP_INTERNAL_SERVER_ERROR}错误代码</p>
	 * 
	 * @return {@linkplain Status 状态}
	 */
	public Status getSpecificPortMappingEntry(int port, Protocol protocol) throws NetException {
		if(!this.available) {
			return Status.uninit;
		}
		UpnpRequest upnpRequest = UpnpRequest.newRequest(this.serviceType);
		String xml = upnpRequest.buildGetSpecificPortMappingEntry(port, protocol);
		var client = HTTPClient.newInstance(this.controlURL);
		var response = client
			.header("SOAPAction", "\"" + this.serviceType + "#GetSpecificPortMappingEntry\"")
			.post(xml, BodyHandlers.ofString());
		String body = response.body();
		if(HTTPClient.internalServerError(response)) {
			return Status.mapable;
		}
		final String registerIp = UpnpResponse.parseGetSpecificPortMappingEntry(body);
		final String localIp = NetUtils.inetHostAddress();
		if(localIp.equals(registerIp)) {
			return Status.useable;
		} else {
			return Status.disable;
		}
	}
	
	/**
	 * <p>添加端口映射：AddPortMapping</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#AddPortMapping"</p>
	 */
	public boolean addPortMapping(int port, int portExt, Protocol protocol) throws NetException {
		if(!this.available) {
			return false;
		}
		final String address = NetUtils.inetHostAddress();
		UpnpRequest upnpRequest = UpnpRequest.newRequest(this.serviceType);
		String xml = upnpRequest.buildAddPortMapping(port, address, portExt, protocol);
		var client = HTTPClient.newInstance(this.controlURL);
		var response = client
			.header("SOAPAction", "\"" + this.serviceType + "#AddPortMapping\"")
			.post(xml, BodyHandlers.ofString());
		return HTTPClient.ok(response);
	}
	
	/**
	 * <p>删除端口映射：DeletePortMapping</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#DeletePortMapping"</p>
	 */
	public boolean deletePortMapping(int port, Protocol protocol) throws NetException {
		if(!this.available) {
			return false;
		}
		UpnpRequest upnpRequest = UpnpRequest.newRequest(this.serviceType);
		String xml = upnpRequest.buildDeletePortMapping(port, protocol);
		var client = HTTPClient.newInstance(this.controlURL);
		var response = client
			.header("SOAPAction", "\"" + this.serviceType + "#DeletePortMapping\"")
			.post(xml, BodyHandlers.ofString());
		return HTTPClient.ok(response);
	}
	
	/**
	 * 设置：本机IP，端口绑定等操作
	 */
	public void setting() throws NetException {
		if(!this.available) {
			return;
		}
		setPortMapping();
		setExternalIpAddress();
	}
	
	/**
	 * 端口释放
	 */
	public void release() {
		if(!this.available) {
			return;
		}
		try {
			boolean dhtOk = this.deletePortMapping(SystemConfig.getTorrentPortExt(), Protocol.udp);
			boolean peerOk = this.deletePortMapping(SystemConfig.getTorrentPortExt(), Protocol.tcp);
			LOGGER.info("端口释放：DHT：{}、Peer：{}", dhtOk, peerOk);
		} catch (Exception e) {
			LOGGER.error("释放UPNP端口异常", e);
		}
	}
	
	/**
	 * 设置控制地址
	 */
	private void controlURL() throws NetException {
		URL url = null;
		try {
			url = new URL(this.location);
		} catch (MalformedURLException e) {
			throw new NetException("端口映射获取控制URL异常：" + this.location, e);
		}
		final StringBuilder builder = new StringBuilder();
		builder.append(url.getProtocol())
			.append("://")
			.append(url.getAuthority())
			.append(this.controlURL);
		this.controlURL = builder.toString();
	}
	
	/**
	 * 端口映射，如果端口被占用，端口+1继续映射。
	 */
	private void setPortMapping() throws NetException {
		Status udpStatus = Status.disable, tcpStatus;
		int portExt = SystemConfig.getTorrentPort();
		while(true) {
			if(portExt >= NetUtils.MAX_PORT) {
				break;
			}
			udpStatus = this.getSpecificPortMappingEntry(portExt, Protocol.udp);
			if(udpStatus == Status.uninit || udpStatus == Status.disable) {
				portExt++;
				continue;
			}
			tcpStatus = this.getSpecificPortMappingEntry(portExt, Protocol.tcp);
			if(udpStatus == tcpStatus) {
				break;
			} else {
				portExt++;
			}
		}
		if(udpStatus == Status.mapable) {
			SystemConfig.setTorrentPortExt(portExt);
			final boolean dhtOk = this.addPortMapping(SystemConfig.getTorrentPort(), portExt, Protocol.udp);
			final boolean peerOk = this.addPortMapping(SystemConfig.getTorrentPort(), portExt, Protocol.tcp);
			LOGGER.info("端口映射（注册）：DHT（{}-{}-{}）、Peer（{}-{}-{}）", SystemConfig.getTorrentPort(), portExt, dhtOk, SystemConfig.getTorrentPort(), portExt, peerOk);
		} else if(udpStatus == Status.useable) {
			SystemConfig.setTorrentPortExt(portExt);
			LOGGER.info("端口映射（可用）：DHT（{}-{}-{}）、Peer（{}-{}-{}）", SystemConfig.getTorrentPort(), portExt, true, SystemConfig.getTorrentPort(), portExt, true);
		} else {
			LOGGER.error("端口映射失败");
		}
	}
	
	/**
	 * 外网IP地址
	 */
	private void setExternalIpAddress() throws NetException {
		final String externalIpAddress = this.getExternalIPAddress();
		LOGGER.info("外网IP地址：{}", externalIpAddress);
		if(this.externalIpAddress == null) {
			this.externalIpAddress = externalIpAddress;
		} else if(!this.externalIpAddress.equals(externalIpAddress)) {
			this.externalIpAddress = externalIpAddress;
		}
	}
	
}
