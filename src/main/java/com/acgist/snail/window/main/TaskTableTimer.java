package com.acgist.snail.window.main;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;

/**
 * 定时任务：刷新任务列表
 */
public class TaskTableTimer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskTableTimer.class);
	
	private MainController controller;
	private ScheduledExecutorService executor;
	
	private static final TaskTableTimer INSTANCE = new TaskTableTimer();
	
	private TaskTableTimer() {
	}
	
	public static final TaskTableTimer getInstance() {
		return INSTANCE;
	}
	
	private Runnable taskTableUpdater = () -> {
		try {
			MainController controller = INSTANCE.controller;
			controller.setTaskTable(DownloaderManager.getInstance().taskTable());
		} catch (Exception e) {
			LOGGER.error("任务列表刷新任务异常", e);
		}
	};

	/**
	 * 新建定时器
	 */
	public void newTimer(MainController controller) {
		this.controller = controller;
		this.executor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				thread.setName("Task Table Updater");
				thread.setDaemon(true);
				return thread;
			}
		});
		this.executor.scheduleAtFixedRate(taskTableUpdater, 0, 4, TimeUnit.SECONDS);
	}

	/**
	 * 关闭定时器
	 */
	public void shutdown() {
		this.executor.shutdown();
	}
	
}
