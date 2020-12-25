package com.acgist.snail.downloader.torrent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.initializer.impl.TorrentInitializer;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.extend.event.TorrentEvent;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.pojo.wrapper.MultifileSelectorWrapper;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.protocol.torrent.TorrentProtocol;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

public class TorrentDownloaderTest extends Performance {

	@Test
	public void testTorrentDownloaderBuild() throws DownloadException {
		final String url = "E://snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
		ProtocolManager.getInstance().register(TorrentProtocol.getInstance()).available(true);
		final var torrent = TorrentManager.loadTorrent(url);
		final var list = torrent.getInfo().files().stream()
			.map(TorrentFile::path)
			.collect(Collectors.toList());
		GuiManager.register(TorrentEvent.getInstance());
		GuiManager.getInstance().files(MultifileSelectorWrapper.newEncoder(list).serialize());
		final var taskSession = TorrentProtocol.getInstance().buildTaskSession(url);
		final var downloader = taskSession.buildDownloader();
//		downloader.run(); // 不下载
		assertNotNull(downloader);
		final var file = new File(taskSession.getFile());
		assertTrue(file.exists());
		FileUtils.delete(taskSession.getFile());
	}

	@Test
	public void testTorrentDownloader() throws DownloadException {
		if(SKIP) {
			this.log("跳过Torrent下载测试");
			return;
		}
		TorrentInitializer.newInstance().sync();
		TrackerManager.getInstance().register();
		final String url = "E://snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
		ProtocolManager.getInstance().register(TorrentProtocol.getInstance()).available(true);
		final var torrent = TorrentManager.loadTorrent(url);
		final var list = torrent.getInfo().files().stream()
			.map(TorrentFile::path)
			.collect(Collectors.toList());
		GuiManager.register(TorrentEvent.getInstance());
		GuiManager.getInstance().files(MultifileSelectorWrapper.newEncoder(list).serialize());
		final var taskSession = TorrentProtocol.getInstance().buildTaskSession(url);
		final var downloader = taskSession.buildDownloader();
		downloader.run();
		final var file = new File(taskSession.getFile());
		assertTrue(file.exists());
		assertTrue(ArrayUtils.isNotEmpty(file.list()));
		FileUtils.delete(taskSession.getFile());
	}
	
}
