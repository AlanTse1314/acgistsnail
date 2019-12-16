package com.acgist.snail.downloader.http;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.SingleFileDownloader;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>HTTP下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class HttpDownloader extends SingleFileDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloader.class);
	
	private HttpDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * <p>创建HTTP下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return HTTP下载器对象
	 */
	public static final HttpDownloader newInstance(ITaskSession taskSession) {
		return new HttpDownloader(taskSession);
	}
	
	@Override
	public void release() {
		IoUtils.close(this.input);
		IoUtils.close(this.output);
		super.release();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see {@link HttpHeaderWrapper#RANGE}
	 */
	@Override
	protected void buildInput() {
		// 已下载大小
		final long size = FileUtils.fileSize(this.taskSession.getFile());
		// HTTP客户端
		final var client = HTTPClient.newInstance(this.taskSession.getUrl(), SystemConfig.CONNECT_TIMEOUT, SystemConfig.DOWNLOAD_TIMEOUT);
		HttpResponse<InputStream> response = null; // 响应
		try {
			response = client
				.header(HttpHeaderWrapper.RANGE, "bytes=" + size + "-")
				.get(BodyHandlers.ofInputStream());
		} catch (NetException e) {
			LOGGER.error("HTTP请求异常", e);
			fail("HTTP请求失败：" + e.getMessage());
			return;
		}
		if(HTTPClient.ok(response) || HTTPClient.partialContent(response)) {
			final var headers = HttpHeaderWrapper.newInstance(response.headers());
			this.input = new BufferedInputStream(response.body());
			if(headers.range()) { // 支持断点续传
				final long begin = headers.beginRange();
				if(size != begin) {
					LOGGER.warn(
						"HTTP下载错误（已下载大小和开始下载位置不符），开始位置：{}，响应位置：{}，HTTP响应头部：{}",
						size, begin,
						headers.allHeaders()
					);
				}
				this.taskSession.downloadSize(size);
			} else {
				this.taskSession.downloadSize(0L);
			}
		} else if(HTTPClient.requestedRangeNotSatisfiable(response)) {
			if(this.taskSession.downloadSize() == this.taskSession.getSize()) {
				this.complete = true;
			} else {
				fail("无法满足文件下载范围：" + size);
			}
		} else {
			if(response == null) {
				fail("HTTP请求失败");
			} else {
				fail("HTTP请求失败（" + response.statusCode() + "）");
			}
		}
	}

}
