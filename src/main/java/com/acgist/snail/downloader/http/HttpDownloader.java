package com.acgist.snail.downloader.http;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse.BodyHandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.module.config.DownloadConfig;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.utils.HttpUtils;

/**
 * 端点续传：https://www.cnblogs.com/findumars/p/5745345.html
 * 请求数据：
 * 		range: bytes=begin-end
 * 		content-range: bytes begin-end/total
 * 测试：http://my.163.com/zmb/
 */
public class HttpDownloader extends AbstractDownloader implements IDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloader.class);
	
	private byte[] bytes;
	private InputStream input;
	private OutputStream output;
	
	public HttpDownloader(TaskWrapper wrapper) {
		super(wrapper);
	}
		
	@Override
	public void open() {
		bytes = new byte[DownloadConfig.getDownloadBuffer() * 1024];
		try {
			output = new FileOutputStream(wrapper.getFile());
		} catch (FileNotFoundException e) {
			LOGGER.error("打开下载文件流失败", e);
			fail();
		}
		HttpClient client = HttpUtils.newClient();
		var request = HttpUtils.newRequest(this.wrapper.getUrl()).GET().build();
		var response = HttpUtils.request(client, request, BodyHandlers.ofInputStream());
		if(HttpUtils.ok(response)) {
			input = response.body();
		} else {
			fail();
		}
	}
	
	@Override
	public void download() throws IOException {
		int index = 0;
		long begin, end;
		while(true) {
			begin = System.currentTimeMillis();
			index = input.read(bytes);
			if(index == -1) {
				break;
			}
			output.write(bytes, 0, index);
			end = System.currentTimeMillis();
			yield(end - begin);
		}
	}

	@Override
	public void release() {
		try {
			input.close();
		} catch (IOException e) {
			LOGGER.error("关闭输入流异常", e);
		}
		try {
			output.close();
		} catch (IOException e) {
			LOGGER.error("关闭文件流失败", e);
		}
	}
	
}
