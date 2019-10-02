package com.acgist.snail.downloader.ftp;

import java.io.BufferedInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.SingleFileDownloader;
import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.net.ftp.bootstrap.FtpClientBuilder;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>FTP下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class FtpDownloader extends SingleFileDownloader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FtpDownloader.class);

	/**
	 * <p>FTP客户端</p>
	 */
	private FtpClient client;
	
	private FtpDownloader(TaskSession taskSession) {
		super(new byte[128 * 1024], taskSession);
	}

	public static final FtpDownloader newInstance(TaskSession taskSession) {
		return new FtpDownloader(taskSession);
	}

	@Override
	public void release() {
		if(this.client != null) {
			this.client.close();
		}
//		IoUtils.close(this.input); // FtpClient关闭
		IoUtils.close(this.output);
	}

	@Override
	protected void buildInput() {
		final var entity = this.taskSession.entity();
		// 获取已下载大小
		final long size = FileUtils.fileSize(entity.getFile());
		// 创建FTP客户端
		this.client = FtpClientBuilder.newInstance(entity.getUrl()).build();
		final boolean ok = this.client.connect();
		if(ok) {
			try {
				final var inputStream = this.client.download(size);
				this.input = new BufferedInputStream(inputStream);
				if(this.client.range()) {
					this.taskSession.downloadSize(size);
				} else {
					this.taskSession.downloadSize(0L);
				}
			} catch (NetException e) {
				fail("FTP下载失败：" + e.getMessage());
				LOGGER.error("FTP下载异常", e);
			}
		} else {
			fail("FTP服务器连接失败");
		}
	}
	
}
