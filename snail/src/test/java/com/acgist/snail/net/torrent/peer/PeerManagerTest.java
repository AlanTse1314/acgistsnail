package com.acgist.snail.net.torrent.peer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.PeerConfig.Source;
import com.acgist.snail.context.SystemStatistics;
import com.acgist.snail.utils.Performance;

public class PeerManagerTest extends Performance {

	@Test
	public void testNewPeerSession() {
		final String hash = "1".repeat(20);
		this.costed(100000, 100, () -> {
			PeerManager.getInstance().newPeerSession(hash, SystemStatistics.getInstance().statistics(), "192.168.1.100", 1000, Source.CONNECT);
		});
		this.log(PeerManager.getInstance().listPeerSession(hash).size());
		assertTrue(PeerManager.getInstance().listPeerSession(hash).size() == 1);
	}
	
}
