package com.acgist.snail.pojo.session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.gui.main.TaskDisplay;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>Statistics Session</p>
 * <p>下载统计：速度、限速、统计等</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class StatisticsSession {

	private static final long ONE_SECOND = 1000L; // 一秒钟
	/**
	 * 下载速度、上传速度缓存时间：默认是刷新频率
	 */
	private static final long CACHE_SECOND = TaskDisplay.INTERVAL.toMillis();
	
	private final boolean limit; // 限速开关
	private final StatisticsSession parent; // 父类统计
	
	private AtomicLong uploadSize = new AtomicLong(0); // 已上传大小
	private AtomicLong downloadSize = new AtomicLong(0); // 已下载大小
	
	private long lastDownloadTime = System.currentTimeMillis(); // 最后一次统计时间
	private long downloadSecond = 0L; // 每秒下载速度
	private AtomicLong downloadBuffer = new AtomicLong(0); // 下载速度采样
	
	private long lastUploadTime = System.currentTimeMillis(); // 最后一次统计时间
	private long uploadSecond = 0L; // 每秒下载速度
	private AtomicLong uploadBuffer = new AtomicLong(0); // 下载速度采样
	
	public StatisticsSession() {
		this.limit = false;
		this.parent = null;
	}
	
	public StatisticsSession(StatisticsSession parent) {
		this.limit = false;
		this.parent = parent;
	}
	
	public StatisticsSession(boolean limit, StatisticsSession parent) {
		this.limit = limit;
		this.parent = parent;
	}

	/**
	 * 下载统计，如果存在父类更新父类数据
	 */
	public void download(long buffer) {
		this.downloadSize.addAndGet(buffer);
		limitDownload(buffer);
		if(this.parent != null) {
			this.parent.download(buffer);
		}
	}
	
	/**
	 * 下载统计，如果存在父类更新父类数据
	 */
	public void upload(long buffer) {
		this.uploadSize.addAndGet(buffer);
		limitUpload(buffer);
		if(this.parent != null) {
			this.parent.upload(buffer);
		}
	}
	
	/**
	 * 下载速度，超过一定时间设置：0
	 */
	public long downloadSecond() {
		if(System.currentTimeMillis() - this.lastDownloadTime > CACHE_SECOND) {
			this.downloadSecond = 0L;
		}
		return this.downloadSecond;
	}
	
	/**
	 * 上传速度，超过一定时间设置：0
	 */
	public long uploadSecond() {
		if(System.currentTimeMillis() - this.lastUploadTime > CACHE_SECOND) {
			this.uploadSecond = 0L;
		}
		return this.uploadSecond;
	}
	
	/**
	 * 获取累计下载大小
	 */
	public long downloadSize() {
		return this.downloadSize.get();
	}
	
	/**
	 * 设置累计下载大小
	 */
	public void downloadSize(long size) {
		this.downloadSize.set(size);
	}
	
	/**
	 * 获取累计上传大小
	 */
	public long uploadSize() {
		return this.uploadSize.get();
	}

	/**
	 * 设置累计上传大小
	 */
	public void uploadSize(long size) {
		this.uploadSize.set(size);
	}
	
	/**
	 * 下载秒速统计限制
	 */
	private void limitDownload(long buffer) {
		final int limitSize = DownloadConfig.getBufferByte();
		final long oldDownloadBuffer = this.downloadBuffer.addAndGet(buffer);
		long interval = System.currentTimeMillis() - this.lastDownloadTime; // 时间间隔
		if(oldDownloadBuffer > limitSize || interval >= ONE_SECOND) { // 超过限速获取时间超过一秒钟
			synchronized (this) {
				if(oldDownloadBuffer == this.downloadBuffer.get()) {
					if(this.limit) {
						final long expectTime = BigDecimal.valueOf(oldDownloadBuffer)
							.multiply(BigDecimal.valueOf(ONE_SECOND))
							.divide(BigDecimal.valueOf(limitSize), RoundingMode.HALF_UP)
							.longValue();
						if(interval < expectTime) { // 限速
							ThreadUtils.sleep(expectTime - interval);
							interval = expectTime;
						}
					}
					this.downloadSecond = oldDownloadBuffer * 1000 / interval;
					this.lastDownloadTime = System.currentTimeMillis();
					this.downloadBuffer.set(0); // 清空
				}
			}
		}
	}
	
	/**
	 * 上传秒速统计限制：上传限速=下载限速
	 */
	private void limitUpload(long buffer) {
		final int limitSize = DownloadConfig.getBufferByte();
		final long oldUploadBuffer = this.uploadBuffer.addAndGet(buffer);
		long interval = System.currentTimeMillis() - this.lastUploadTime;
		if(oldUploadBuffer > limitSize || interval >= ONE_SECOND) { // 超过限速
			synchronized (this) {
				if(oldUploadBuffer == this.uploadBuffer.get()) {
					if(this.limit) {
						final long expectTime = BigDecimal.valueOf(oldUploadBuffer)
							.multiply(BigDecimal.valueOf(ONE_SECOND))
							.divide(BigDecimal.valueOf(limitSize), RoundingMode.HALF_UP)
							.longValue();
						if(interval < expectTime) { // 限速
							ThreadUtils.sleep(expectTime - interval);
							interval = expectTime;
						}
					}
					this.uploadSecond = oldUploadBuffer * 1000 / interval;
					this.lastUploadTime = System.currentTimeMillis();
					this.uploadBuffer.set(0); // 清空
				}
			}
		}
	}
	
}
