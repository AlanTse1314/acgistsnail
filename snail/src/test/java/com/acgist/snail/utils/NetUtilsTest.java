package com.acgist.snail.utils;

import java.net.NetworkInterface;
import java.net.SocketException;

import org.junit.jupiter.api.Test;

public class NetUtilsTest extends Performance {

	@Test
	public void ip() {
		this.log(NetUtils.decodeLongToIp(2130706433));
		this.log(NetUtils.encodeIpToLong("127.1.1.1"));
	}
	
	@Test
	public void testAddress() throws SocketException {
		int mask = 24;
		mask = (-1 >> (31 - (mask - 1))) << (31 - (mask - 1));
		this.log(NetUtils.decodeIntToIp(mask));
		NetworkInterface.networkInterfaces().forEach(x -> {
			x.getInterfaceAddresses().stream().forEach(i -> {
				this.log("地址：" + i);
				final var v = i.getAddress();
				this.log(i.getBroadcast());
				this.log(i.getNetworkPrefixLength());
				this.log(v.isAnyLocalAddress());
				this.log(v.isLoopbackAddress());
				this.log(v.isLinkLocalAddress());
				this.log(v.isSiteLocalAddress());
				this.log(v.isMulticastAddress());
			});
			this.log(x);
		});
		this.log(NetUtils.localHostName());
		this.log(NetUtils.localHostAddress());
		this.log(NetUtils.defaultNetworkInterface());
	}
	
	@Test
	public void testIPv6() {
		String ipv6 = "fe80::f84b:bc3a:9556:683d";
		byte[] bytes = NetUtils.encodeIPv6(ipv6);
		this.log(StringUtils.hex(bytes));
		String value = NetUtils.decodeIPv6(bytes);
		this.log(value);
	}
	
}
