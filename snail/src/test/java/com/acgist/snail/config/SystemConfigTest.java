package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.Performance;

public class SystemConfigTest extends Performance {

	@Test
	public void test() {
		final int port = 38888;
		final short portShort = NetUtils.portToShort(port);
		assertEquals(SystemConfig.getNameEnAndVersion(), SystemConfig.getNameEn() + " " + SystemConfig.getVersion());
		assertEquals(18888, SystemConfig.getTorrentPort());
		SystemConfig.setTorrentPortExt(port);
		assertEquals(port, SystemConfig.getTorrentPortExt());
		assertEquals(portShort, SystemConfig.getTorrentPortExtShort());
		assertEquals(port, NetUtils.portToInt(portShort));
	}
	
}
