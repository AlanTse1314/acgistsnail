package com.acgist.snail;

import org.junit.Test;

import com.acgist.snail.net.stun.StunClient;
import com.acgist.snail.system.config.StunConfig;
import com.acgist.snail.system.config.StunConfig.MethodType;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ThreadUtils;

public class StunTest {

	@Test
	public void config() {
		short value = StunConfig.MessageType.REQUEST.type(MethodType.BINDING);
		System.out.println(value);
		System.out.println(String.format("%016d", Integer.valueOf(Integer.toBinaryString(value))));
		System.out.println(StunConfig.MessageType.valueOf(value));
		value = StunConfig.MessageType.INDICATION.type(MethodType.BINDING);
		System.out.println(value);
		System.out.println(String.format("%016d", Integer.valueOf(Integer.toBinaryString(value))));
		System.out.println(StunConfig.MessageType.valueOf(value));
		value = StunConfig.MessageType.SUCCESS_RESPONSE.type(MethodType.BINDING);
		System.out.println(value);
		System.out.println(String.format("%016d", Integer.valueOf(Integer.toBinaryString(value))));
		System.out.println(StunConfig.MessageType.valueOf(value));
		value = StunConfig.MessageType.ERROR_RESPONSE.type(MethodType.BINDING);
		System.out.println(value);
		System.out.println(String.format("%016d", Integer.valueOf(Integer.toBinaryString(value))));
		System.out.println(StunConfig.MessageType.valueOf(value));
	}
	
	@Test
	public void mappedAddress() {
//		StunClient client = StunClient.newInstance("stun.l.google.com", 19302);
//		StunClient client = StunClient.newInstance("stun1.l.google.com", 19302);
//		StunClient client = StunClient.newInstance("stun2.l.google.com", 19302);
//		StunClient client = StunClient.newInstance("stun3.l.google.com", 19302);
//		StunClient client = StunClient.newInstance("stun4.l.google.com", 19302);
//		StunClient client = StunClient.newInstance("stun.xten.com");
//		StunClient client = StunClient.newInstance("stunserver.org");
		StunClient client = StunClient.newInstance("numb.viagenie.ca");
//		StunClient client = StunClient.newInstance("stun.softjoys.com");
		client.mappedAddress();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	@Test
	public void xor() {
		short port = 4938;
		int ip = -1777019015;
		int realPort = port ^ (StunConfig.MAGIC_COOKIE >> 16);
		System.out.println(realPort);
		int realIp = ip ^ StunConfig.MAGIC_COOKIE;
		System.out.println(realIp);
		System.out.println(NetUtils.decodeIntToIp(realIp));
	}
	
}
