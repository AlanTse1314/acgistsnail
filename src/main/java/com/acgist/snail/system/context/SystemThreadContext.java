package com.acgist.snail.system.context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 系统线程
 */
public class SystemThreadContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemThreadContext.class);
	
	private static final String THREAD_POOL_NAME = "Snail Thread";
	private static final String TIMER_THREAD_POOL_NAME = "Snail Timer Thread";
	
	/**
	 * 线程池
	 */
	private static final ExecutorService EXECUTOR;
	/**
	 * 定时线程池
	 */
	private static final ScheduledExecutorService TIMER_EXECUTOR;
	
	static {
		LOGGER.info("启动系统线程池");
		EXECUTOR = new ThreadPoolExecutor(
			4, // 初始线程数量
			100, // 最大线程数量
			60L, // 空闲时间
			TimeUnit.SECONDS, // 空闲时间单位
			new SynchronousQueue<Runnable>(), // 等待线程
			SystemThreadContext.newThreadFactory(THREAD_POOL_NAME) // 线程工厂
		);
		TIMER_EXECUTOR = Executors.newScheduledThreadPool(
			2,
			SystemThreadContext.newThreadFactory(TIMER_THREAD_POOL_NAME)
		);
	}
	
	/**
	 * 异步执行线程：<br>
	 * 处理一些比较消耗资源，导致卡住窗口的操作，例如：文件校验
	 */
	public static final void submit(Runnable runnable) {
		EXECUTOR.submit(runnable);
	}
	
	/**
	 * 定时任务（重复）
	 */
	public static final void timer(long delay, long period, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			TIMER_EXECUTOR.scheduleAtFixedRate(runnable, delay, period, unit);
		}
	}

	/**
	 * 定时任务（不重复）
	 */
	public static final void timer(long delay, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			TIMER_EXECUTOR.schedule(runnable, delay, unit);
		}
	}
	
	/**
	 * 获取系统线程池
	 */
	public static final ExecutorService executor() {
		return EXECUTOR;
	}
	
	/**
	 * 新建线程池工厂
	 * @param poolName 线程池名称
	 */
	private static final ThreadFactory newThreadFactory(String poolName) {
		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				thread.setName(poolName);
				thread.setDaemon(true);
				return thread;
			}
		};
	}
	
	/**
	 * 关闭线程
	 */
	public static final void shutdown() {
		LOGGER.info("关闭系统线程池");
		shutdown(EXECUTOR);
		shutdown(TIMER_EXECUTOR);
	}
	
	/**
	 * 关闭线程池
	 */
	public static final void shutdown(ExecutorService executor) {
		if(executor != null && !executor.isShutdown()) {
			executor.shutdown();
			try {
				executor.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				LOGGER.error("线程池关闭等待异常", e);
			}
		}
	}
	
}
