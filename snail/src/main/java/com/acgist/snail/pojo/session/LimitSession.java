package com.acgist.snail.pojo.session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>限速信息</p>
 * 
 * @author acgist
 */
public final class LimitSession {

	/**
	 * <p>限制类型</p>
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		UPLOAD,
		DOWNLOAD;
		
	}
	
	/**
	 * <p>限制类型</p>
	 */
	private final Type type;
	/**
	 * <p>限速采样</p>
	 */
	private final AtomicLong limitBuffer;
	/**
	 * <p>限速最后一次采样时间</p>
	 */
	private volatile long lastLimitTime;
	
	/**
	 * @param type 限制类型
	 */
	public LimitSession(Type type) {
		this.type = type;
		this.limitBuffer = new AtomicLong(0);
		this.lastLimitTime = System.currentTimeMillis();
	}
	
	/**
	 * <p>限制速度</p>
	 * 
	 * @param buffer 数据大小
	 */
	public void limit(long buffer) {
		final long maxLimitBuffer = this.maxLimitBuffer();
		final long interval = System.currentTimeMillis() - this.lastLimitTime;
		final long limitBuffer = this.limitBuffer.addAndGet(buffer);
		if(limitBuffer >= maxLimitBuffer || interval >= DateUtils.ONE_SECOND) { // 限速控制
			synchronized (this.limitBuffer) { // 阻塞其他线程
				if(limitBuffer == this.limitBuffer.get()) { // 验证
					// 期望时间：更加精确：可以使用一秒
					final long expectTime = BigDecimal.valueOf(limitBuffer)
						.multiply(BigDecimal.valueOf(DateUtils.ONE_SECOND))
						.divide(BigDecimal.valueOf(maxLimitBuffer), RoundingMode.HALF_UP)
						.longValue();
					if(interval < expectTime) { // 限速时间
						ThreadUtils.sleep(expectTime - interval);
					}
					this.limitBuffer.set(0); // 清零：不能在休眠前清零
					this.lastLimitTime = System.currentTimeMillis();
				} else { // 防止误差
					this.limitBuffer.addAndGet(buffer);
				}
			}
		}
	}
	
	/**
	 * <p>获取限制速度</p>
	 * 
	 * @return 限制速度
	 */
	private long maxLimitBuffer() {
		if(type == Type.UPLOAD) {
			return DownloadConfig.getUploadBufferByte();
		} else {
			return DownloadConfig.getDownloadBufferByte();
		}
	}
	
}
