package com.acgist.snail.torrent;

import java.util.List;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.protocol.magnet.bootstrap.TorrentBuilder;
import com.acgist.snail.system.exception.DownloadException;

public class TorrentBuilderTest extends BaseTest {

	@Test
	public void testBuild() throws DownloadException {
		final var path = "e:/snail/12345.torrent";
		final var session = TorrentManager.getInstance().newTorrentSession(path);
		final var infoHash = session.infoHash();
		this.log("HASH：{}", infoHash.infoHashHex());
		NodeManager.getInstance().newNodeSession("12345678901234567890".getBytes(), "192.168.1.1", 18888);
		final var trackers = List.of("https://www.acgist.com", "https://www.acgist.com/1", "https://www.acgist.com/2");
		final var builder = TorrentBuilder.newInstance(infoHash, trackers);
		builder.buildFile("e:/tmp/torrent");
	}
	
}
