package com.acgist.snail.net.peer.dht;

import java.nio.ByteBuffer;

import com.acgist.snail.downloader.torrent.bootstrap.DhtLauncher;
import com.acgist.snail.net.peer.PeerMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.PeerMessageConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.NetUtils;

/**
 * DHT Protocol
 * http://www.bittorrent.org/beps/bep_0005.html
 */
public class DhtExtensionMessageHandler {
	
	private final DhtLauncher dhtLauncher;
	private final PeerSession peerSession;
	private final PeerMessageHandler peerMessageHandler;

	public static final DhtExtensionMessageHandler newInstance(PeerSession peerSession, DhtLauncher dhtLauncher, PeerMessageHandler peerMessageHandler) {
		return new DhtExtensionMessageHandler(peerSession, dhtLauncher, peerMessageHandler);
	}
	
	private DhtExtensionMessageHandler(PeerSession peerSession, DhtLauncher dhtLauncher, PeerMessageHandler peerMessageHandler) {
		this.peerSession = peerSession;
		this.dhtLauncher = dhtLauncher;
		this.peerMessageHandler = peerMessageHandler;
	}
	
	public void onMessage(ByteBuffer buffer) {
		port(buffer);
	}

	public void port() {
		final short port = NetUtils.encodePort(SystemConfig.getDhtPort());
		this.peerMessageHandler.pushMessage(PeerMessageConfig.Type.dht, ByteBuffer.allocate(2).putShort(port).array());
	}
	
	private void port(ByteBuffer buffer) {
		final int port = NetUtils.decodePort(buffer.getShort());
		this.peerSession.dhtPort(port);
		dhtLauncher.put(peerSession.host(), port);
	}

}
