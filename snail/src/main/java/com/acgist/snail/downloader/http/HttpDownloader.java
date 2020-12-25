package com.acgist.snail.downloader.http;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.downloader.SingleFileDownloader;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>HTTP任务下载器</p>
 * 
 * @author acgist
 */
public final class HttpDownloader extends SingleFileDownloader {

	/**
	 * @param taskSession 任务信息
	 */
	private HttpDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * <p>创建HTTP任务下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return {@link HttpDownloader}
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
	 * @see HttpHeaderWrapper#HEADER_RANGE
	 */
	@Override
	protected void buildInput() throws NetException {
		// 已下载大小
		final long size = FileUtils.fileSize(this.taskSession.getFile());
		// HTTP客户端
		final var client = HTTPClient.newInstance(this.taskSession.getUrl(), SystemConfig.CONNECT_TIMEOUT, SystemConfig.DOWNLOAD_TIMEOUT);
		// HTTP响应
		final HttpResponse<InputStream> response = client.range(size).get(BodyHandlers.ofInputStream());
		// 请求成功和部分请求成功
		if(
			HTTPClient.StatusCode.OK.verifyCode(response) ||
			HTTPClient.StatusCode.PARTIAL_CONTENT.verifyCode(response)
		) {
			final var headers = HttpHeaderWrapper.newInstance(response.headers());
			if(headers.range()) { // 支持断点续传
				headers.verifyBeginRange(size);
				this.input = new BufferedInputStream(response.body(), SystemConfig.DEFAULT_EXCHANGE_BYTES_LENGTH);
				this.taskSession.downloadSize(size);
			} else {
				this.taskSession.downloadSize(0L);
			}
		} else if(HTTPClient.StatusCode.REQUESTED_RANGE_NOT_SATISFIABLE.verifyCode(response)) {
			if(this.taskSession.downloadSize() == this.taskSession.getSize()) {
				this.complete = true;
			} else {
				this.fail("无法满足文件下载范围：" + size);
			}
		} else {
			if(response == null) {
				this.fail("HTTP请求失败");
			} else {
				this.fail("HTTP请求失败：" + response.statusCode());
			}
		}
	}

}
