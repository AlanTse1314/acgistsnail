package com.acgist.snail.window.menu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.window.main.MainController;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

/**
 * 任务菜单
 */
public class TaskMenu extends ContextMenu {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskMenu.class);
	
	private MainController mainController;
	
	private static TaskMenu INSTANCE;
	
	private TaskMenu() {
		createMenu();
	}

	static {
		synchronized (TaskMenu.class) {
			if (INSTANCE == null) {
				LOGGER.info("初始化任务菜单");
				INSTANCE = new TaskMenu();
			}
		}
	}
	
	public static final TaskMenu getInstance(MainController mainController) {
		INSTANCE.mainController = mainController;
		return INSTANCE;
	}

	/**
	 * 创建菜单
	 */
	private void createMenu() {
		MenuItem startMenu = new MenuItem("开始", new ImageView("/image/16/start.png"));
		MenuItem pauseMenu = new MenuItem("暂停", new ImageView("/image/16/pause.png"));
		MenuItem deleteMenu = new MenuItem("删除", new ImageView("/image/16/delete.png"));
		MenuItem detailMenu = new MenuItem("显示详情", new ImageView("/image/16/detail.png"));
		MenuItem copyURLMenu = new MenuItem("复制下载地址", new ImageView("/image/16/download.png"));
		MenuItem exportTorrentMenu = new MenuItem("导出种子", new ImageView("/image/16/export.png"));
		MenuItem openFolderMenu = new MenuItem("打开目录", new ImageView("/image/16/folder.png"));

		startMenu.setOnAction((event) -> {
			mainController.handleStartAction(event);
		});
		
		this.getItems().add(startMenu);
		this.getItems().add(pauseMenu);
		this.getItems().add(deleteMenu);
		this.getItems().add(detailMenu);
		this.getItems().add(copyURLMenu);
		this.getItems().add(exportTorrentMenu);
		this.getItems().add(openFolderMenu);
	}
	
}
