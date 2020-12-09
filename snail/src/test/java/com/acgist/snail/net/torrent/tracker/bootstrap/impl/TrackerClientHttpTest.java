package com.acgist.snail.net.torrent.tracker.bootstrap.impl;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.Performance;

public class TrackerClientHttpTest extends Performance {

	@Test
	public void testAnnounce() throws DownloadException, NetException {
		String path = "E:/snail/12345.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
//		HttpTrackerClient client = HttpTrackerClient.newInstance("http://www.proxmox.com:6969/announce");
		HttpTrackerClient client = HttpTrackerClient.newInstance("http://tracker3.itzmx.com:6961/announce");
//		HttpTrackerClient client = HttpTrackerClient.newInstance("http://opentracker.acgnx.se/announce");
		client.announce(1000, session);
//		client.scrape(1000, session);
	}
	
}
