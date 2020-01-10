package com.acgist.snail.downloader;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>单个文件任务下载器</p>
 * 
 * TODO：分段下载技术（断点续传支持）
 * 
 * @author acgist
 * @since 1.1.1
 */
public abstract class SingleFileDownloader extends Downloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(SingleFileDownloader.class);
	
	/**
	 * <p>下载字节缓存大小：{@value}</p>
	 */
	protected static final int EXCHANGE_BYTES_LENGTH = 16 * SystemConfig.ONE_KB;
	
	/**
	 * <p>输入流</p>
	 */
	protected InputStream input;
	/**
	 * <p>输出流</p>
	 */
	protected OutputStream output;
	
	protected SingleFileDownloader(ITaskSession taskSession) {
		super(taskSession);
	}
	
	@Override
	public void open() {
		buildInput();
		buildOutput();
	}

	@Override
	public void download() throws IOException {
		int length = 0;
		final byte[] bytes = new byte[EXCHANGE_BYTES_LENGTH];
		while(ok()) {
			length = this.input.read(bytes, 0, bytes.length);
			if(isComplete(length)) {
				this.complete = true;
				break;
			}
			this.output.write(bytes, 0, length);
			this.download(length);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>如果没有数据下载，任务会被{@linkplain InputStream#read(byte[], int, int) 下载流读取方法}阻塞，通过直接关闭下载流来避免任务不能正常结束。</p>
	 */
	@Override
	public void unlockDownload() {
		if(!this.taskSession.statistics().downloading()) {
			LOGGER.debug("单个文件下载解锁：没有速度关闭下载流");
			IoUtils.close(this.input);
		}
	}
	
	/**
	 * <dl>
	 * 	<dt>判断任务是否下载完成</dt>
	 * 	<dd>读取数据长度小于等于{@code -1}</dd>
	 * 	<dd>任务长度小于等于已下载数据长度</dd>
	 * </dl>
	 * 
	 * @param length 数据长度
	 * 
	 * @return {@code true}-下载完成；{@code false}-没有完成；
	 */
	protected boolean isComplete(int length) {
		return
			length <= -1 ||
			this.taskSession.getSize() <= this.taskSession.downloadSize();
	}
	
	/**
	 * <p>创建{@linkplain #output 输出流}</p>
	 * <p>创建时需要验证是否支持断点续传所以优先{@linkplain #buildInput() 创建输入流}</p>
	 */
	protected void buildOutput() {
		try {
			final long size = this.taskSession.downloadSize();
			if(size == 0L) { // 不支持断点续传
				this.output = new BufferedOutputStream(new FileOutputStream(this.taskSession.getFile()), DownloadConfig.getMemoryBufferByte());
			} else { // 支持断点续传
				this.output = new BufferedOutputStream(new FileOutputStream(this.taskSession.getFile(), true), DownloadConfig.getMemoryBufferByte());
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("下载文件打开异常", e);
			fail("下载文件打开失败：" + e.getMessage());
		}
	}
	
	/**
	 * <p>创建{@linkplain #input 输入流}</p>
	 * <p>先验证是否支持断点续传，如果支持重新设置任务已下载大小。</p>
	 */
	protected abstract void buildInput();

}
