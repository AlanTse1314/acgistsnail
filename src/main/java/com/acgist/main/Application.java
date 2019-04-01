package com.acgist.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.main.MainWindow;
import com.acgist.snail.gui.menu.TrayMenu;
import com.acgist.snail.system.context.SystemContext;

import javafx.application.Platform;

/**
 * 系统启动
 * TODO：启动检测、端口、外网端口
 */
public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	
	public static final void main(String[] args) {
		LOGGER.info("系统开始启动");
		SystemContext.info();
		final boolean enable = listen();
		if(enable) {
			initSystem();
			buildWindow();
		}
		LOGGER.info("系统启动完成");
	}
	
	/**
	 * 启动系统监听
	 */
	private static final boolean listen() {
		return SystemContext.listen();
	}
	
	/**
	 * 系统初始化
	 */
	private static final void initSystem() {
		SystemContext.init();
	}
	
	/**
	 * 创建窗口
	 */
	private static final void buildWindow() {
		LOGGER.info("初始化窗口");
		Thread thread = new Thread();
		thread.setName("Snail Platform Thread");
		thread.setDaemon(true);
		Platform.startup(thread);
		Platform.runLater(() -> {
			TrayMenu.getInstance();
			MainWindow.getInstance().show();
		});
	}

}