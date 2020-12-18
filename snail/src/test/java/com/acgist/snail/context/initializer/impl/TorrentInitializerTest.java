package com.acgist.snail.context.initializer.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.Performance;

public class TorrentInitializerTest extends Performance {

	@Test
	public void testTorrentInitializer() throws IOException {
		TorrentInitializer.newInstance().sync();
		final Socket socket = new Socket();
		socket.connect(new InetSocketAddress(SystemConfig.getTorrentPort()));
		assertTrue(socket.isConnected());
		socket.close();
	}
	
}
