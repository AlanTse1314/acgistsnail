package com.acgist.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.config.SystemConfig;
import com.acgist.snail.module.initializer.SystemInitializer;
import com.acgist.snail.utils.PlatformUtils;
import com.acgist.snail.window.main.MainWindow;

/**
 * 启动类
 */
public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	
	public static void main(String[] args) {
		LOGGER.info("系统开始启动");
		if(listen()) {
			init();
			buildWindow(args);
		}
		LOGGER.info("系统启动完成");
	}
	
	/**
	 * 启动系统监听
	 */
	private static final boolean listen() {
		LOGGER.info("启动系统监听");
		return PlatformUtils.listen();
	}
	
	/**
	 * 系统初始化
	 */
	private static final void init() {
		LOGGER.info("系统初始化");
		SystemInitializer.init();
	}
	
	/**
	 * 创建窗口
	 */
	private static final void buildWindow(String[] args) {
		LOGGER.info("初始化窗口");
		Thread thread = new Thread(() -> {
			MainWindow.main(args);
		});
		thread.setName(SystemConfig.getName() + "窗口");
		thread.start();
	}
	
}