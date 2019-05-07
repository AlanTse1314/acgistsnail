package com.acgist.snail.system.context;

import com.acgist.snail.pojo.session.StatisticsSession;

/**
 * 系统统计：累计下载、累计上传、速度采样
 */
public class SystemStatistics {
	
	/**
	 * 系统统计
	 */
	private StatisticsSession systemStatistics;
	
	private static final SystemStatistics INSTANCE = new SystemStatistics();
	
	private SystemStatistics() {
		systemStatistics = new StatisticsSession();
	}
	
	public static final SystemStatistics getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 系统统计
	 */
	public StatisticsSession getSystemStatistics() {
		return systemStatistics;
	}
	
	/**
	 * 下载速度
	 */
	public long downloadSecond() {
		return systemStatistics.downloadSecond();
	}
	
	/**
	 * 上传速度
	 */
	public long uploadSecond() {
		return systemStatistics.uploadSecond();
	}
	
}
