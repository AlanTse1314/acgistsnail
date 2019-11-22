package com.acgist.snail.pojo.session;

import java.util.concurrent.atomic.AtomicInteger;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.DateUtils;

/**
 * <p>速度统计</p>
 * 
 * @author acgist
 * @since 1.2.2
 */
public final class SpeedSession {
	
	/**
	 * 采样次数
	 */
	private static final byte SAMPLE_SIZE = 10;
	/**
	 * <p>速度采样时间</p>
	 * <p>小于刷新时间：防止统计误差</p>
	 */
	private static final long SAMPLE_TIME = SystemConfig.TASK_REFRESH_INTERVAL.toMillis() - DateUtils.ONE_SECOND;

	/**
	 * 速度
	 */
	private long speed = 0L;
	/**
	 * 最后一次采样时间
	 */
	private long bufferSampleTime;
	/**
	 * 速度采样
	 */
	private final AtomicInteger bufferSample = new AtomicInteger(0);
	/**
	 * 当前采样位置
	 */
	private byte index = 0;
	/**
	 * 速度采样集合
	 */
	private final int[] bufferSamples = new int[SAMPLE_SIZE];
	
	/**
	 * 速度采样
	 * 
	 * @param buffer 数据大小
	 */
	public void buffer(int buffer) {
		this.bufferSample.addAndGet(buffer);
	}

	/**
	 * <p>计算下载速度</p>
	 * <p>超过采样时间：计算速度</p>
	 * <p>小于采样时间：返回上次速度</p>
	 * 
	 * @return 下载速度
	 */
	public synchronized long speed() {
		final long time = System.currentTimeMillis();
		final long interval = time - this.bufferSampleTime;
		if(interval >= SAMPLE_TIME) {
			this.speed = this.calculateSpeed();
			this.bufferSampleTime = time;
		}
		return this.speed;
	}

	/**
	 * <p>计算下载速度</p>
	 */
	private long calculateSpeed() {
		// 采样
		this.bufferSamples[this.index] = this.bufferSample.getAndSet(0);
		if(++this.index >= SAMPLE_SIZE) {
			this.index = 0;
		}
		// 平均速度
		long value = 0L;
		for (int bufferSample : this.bufferSamples) {
			value += (bufferSample * DateUtils.ONE_SECOND);
		}
		// 存在误差
		return value / SAMPLE_SIZE / SAMPLE_TIME;
	}
	
}
