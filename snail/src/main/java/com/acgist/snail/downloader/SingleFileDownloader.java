package com.acgist.snail.downloader;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.StreamContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.session.StreamSession;

/**
 * <p>单文件任务下载器</p>
 * 
 * TODO：分段下载技术（断点续传支持：突破网盘限速）
 * TODO：大文件下载内存优化
 * 
 * @author acgist
 */
public abstract class SingleFileDownloader extends Downloader {
	
	/**
	 * <p>输入流</p>
	 */
	protected InputStream input;
	/**
	 * <p>输出流</p>
	 */
	protected OutputStream output;
	/**
	 * <p>数据流信息</p>
	 */
	private StreamSession streamSession;
	
	/**
	 * @param taskSession 下载任务
	 */
	protected SingleFileDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>单文件任务校验失败修改已下载大小</p>
	 */
	@Override
	public boolean verify() throws DownloadException {
		final boolean verify = super.verify();
		if(!verify) {
			this.taskSession.downloadSize(0L);
		}
		return verify;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>创建{@linkplain #output 输出流}时需要验证服务端是否支持断点续传，所以优先创建{@linkplain #input 输入流}获取服务端信息。</p>
	 */
	@Override
	public void open() throws NetException, DownloadException {
		this.buildInput();
		this.buildOutput();
	}

	@Override
	public void download() throws DownloadException {
		int length = 0;
		final byte[] bytes = new byte[SystemConfig.DEFAULT_EXCHANGE_BYTES_LENGTH];
		this.streamSession = StreamContext.getInstance().newStreamSession(this.input);
		try {
			while(this.downloadable()) {
				length = this.input.read(bytes, 0, bytes.length);
				if(length >= 0) {
					this.output.write(bytes, 0, length);
					this.statistics.download(length);
					this.statistics.downloadLimit(length);
				}
				this.streamSession.heartbeat();
				if(this.checkCompleted(length)) {
					this.complete = true;
					break;
				}
			}
		} catch (Exception e) {
			throw new DownloadException("数据流操作失败", e);
		} finally {
			this.streamSession.remove();
		}
	}

	@Override
	public void unlockDownload() {
		super.unlockDownload();
		if(this.streamSession != null) {
			// 快速失败
			this.streamSession.fastCheckLive();
		}
	}
	
	/**
	 * <p>判断是否下载完成</p>
	 * 
	 * @param length 下载数据大小
	 * 
	 * @return 是否下载完成
	 */
	protected boolean checkCompleted(int length) {
		return
			// 没有更多数据
			length <= -1 ||
			// 已下载数据大小大于等于文件大小
			this.taskSession.getSize() <= this.taskSession.downloadSize();
	}
	
	/**
	 * <p>创建{@linkplain #output 输出流}</p>
	 * <p>通过判断任务已下载大小判断是否支持断点续传</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void buildOutput() throws DownloadException {
		try {
			final long size = this.taskSession.downloadSize();
			if(size == 0L) {
				// 不支持断点续传
				this.output = new BufferedOutputStream(new FileOutputStream(this.taskSession.getFile()), DownloadConfig.getMemoryBufferByte());
			} else {
				// 支持断点续传
				this.output = new BufferedOutputStream(new FileOutputStream(this.taskSession.getFile(), true), DownloadConfig.getMemoryBufferByte());
			}
		} catch (FileNotFoundException e) {
			throw new DownloadException("下载文件打开失败", e);
		}
	}
	
	/**
	 * <p>创建{@linkplain #input 输入流}</p>
	 * <p>验证是否支持断点续传，如果支持重新设置任务已下载大小。</p>
	 * 
	 * @throws NetException 网络异常
	 * @throws DownloadException 下载异常
	 */
	protected abstract void buildInput() throws NetException, DownloadException;

}
