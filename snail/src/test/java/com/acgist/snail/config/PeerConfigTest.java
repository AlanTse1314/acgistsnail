package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

public class PeerConfigTest extends BaseTest {

	@Test
	public void testReserved() {
		int value = PeerConfig.RESERVED_DHT_PROTOCOL + PeerConfig.RESERVED_PEER_EXCHANGE + PeerConfig.RESERVED_FAST_PROTOCOL;
		this.log(PeerConfig.RESERVED);
		assertEquals(value, PeerConfig.RESERVED[7]);
		value += PeerConfig.RESERVED_NAT_TRAVERSAL;
		PeerConfig.nat();
		this.log(PeerConfig.RESERVED);
		assertEquals(value, PeerConfig.RESERVED[7]);
		assertEquals(PeerConfig.RESERVED_EXTENSION_PROTOCOL, PeerConfig.RESERVED[5]);
	}
	
	@Test
	public void testClientName() {
		String name = PeerConfig.clientName("-XL1000-xx".getBytes());
		this.log(name);
		assertEquals("Xunlei", name);
		name = PeerConfig.clientName("-AS1000-xx".getBytes());
		this.log(name);
		assertEquals("Acgist Snail", name);
		name = PeerConfig.clientName("S1000-----xx".getBytes());
		this.log(name);
		assertEquals("Shadow's client", name);
	}
	
}
