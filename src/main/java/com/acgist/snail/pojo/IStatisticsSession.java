package com.acgist.snail.pojo;

import com.acgist.snail.system.IStatistics;

/**
 * <p>统计接口</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public interface IStatisticsSession extends IStatistics {

	/**
	 * <p>判断是否在下载数据</p>
	 * <p>最后一次下载限速采样时间是否在一秒内</p>
	 * 
	 * @return 是否下载数据
	 */
	boolean downloading();
	
	/**
	 * {@inheritDoc}
	 * <p>如果存在父类优先更新父类数据，防止限速导致父类更新不及时。</p>
	 */
	@Override
	void upload(int buffer);
	
	/**
	 * {@inheritDoc}
	 * <p>如果存在父类优先更新父类数据，防止限速导致父类更新不及时。</p>
	 */
	@Override
	void download(int buffer);
	
	/**
	 * @return 上传速度
	 */
	long uploadSpeed();
	
	/**
	 * @return 下载速度
	 */
	long downloadSpeed();
	
	/**
	 * @return 累计上传大小
	 */
	long uploadSize();

	/**
	 * @param size 累计上传大小
	 */
	void uploadSize(long size);
	
	/**
	 * @return 累计下载大小
	 */
	long downloadSize();
	
	/**
	 * @param size 累计下载大小
	 */
	void downloadSize(long size);
	
}
