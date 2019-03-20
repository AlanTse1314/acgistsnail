package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.net.tracker.TrackerClientManager;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.TorrentSessionManager;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class TrackerClientTest {

	@Test
	public void test() throws NetException, DownloadException {
		String path = "e:/snail/82309348090ecbec8bf509b83b30b78a8d1f6454.torrent";
		TorrentSession session = TorrentSessionManager.getInstance().buildSession(path);
		System.out.println(session.infoHash().hashHex());
		var client = TrackerClientManager.getInstance().tracker("udp://exodus.desync.com:6969/announce");
		client.announce(session);
		ThreadUtils.sleep(1000000);
	}
	
}
