package com.acgist.snail.downloader.hls;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.protocol.hls.HlsProtocol;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

public class HlsDownloaderTest extends Performance {

	@Test
	public void testHlsDownloaderBuild() throws DownloadException {
//		final String url = "http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8"; // 流媒体
		final String url = "https://iqiyi.cdn9-okzy.com/20201004/16201_5314e9ac/index.m3u8";
//		final String url = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8";
		ProtocolManager.getInstance().register(HlsProtocol.getInstance()).available(true);
		final var taskSession = HlsProtocol.getInstance().buildTaskSession(url);
		final var downloader = taskSession.buildDownloader();
//		downloader.run(); // 不下载
		assertNotNull(downloader);
		final var file = new File(taskSession.getFile());
		assertTrue(file.exists());
		FileUtils.delete(taskSession.getFile());
	}
	
	@Test
	public void testHlsDownloader() throws DownloadException {
		if(SKIP) {
			this.log("跳过HLS下载测试");
			return;
		}
//		final String url = "http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8"; // 流媒体
		final String url = "https://iqiyi.cdn9-okzy.com/20201004/16201_5314e9ac/index.m3u8";
//		final String url = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8";
		ProtocolManager.getInstance().register(HlsProtocol.getInstance()).available(true);
		final var taskSession = HlsProtocol.getInstance().buildTaskSession(url);
		final var downloader = taskSession.buildDownloader();
		downloader.run();
		final var file = new File(taskSession.getFile());
		assertTrue(file.exists());
		assertTrue(ArrayUtils.isNotEmpty(file.list()));
		FileUtils.delete(taskSession.getFile());
	}
	
}
