package com.acgist.snail.gui.menu;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.event.MouseInputAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Menu;
import com.acgist.snail.gui.about.AboutWindow;
import com.acgist.snail.gui.main.MainWindow;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemContext;
import com.acgist.snail.utils.BrowseUtils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * 托盘菜单
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TrayMenu extends Menu {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrayMenu.class);
	
	private static final TrayMenu INSTANCE;
	
	/**
	 * 窗口高度
	 */
	private static final int MENU_WINDOW_HEIGHT = 150;
	
	static {
		LOGGER.debug("初始化托盘菜单");
		INSTANCE = new TrayMenu();
		// 必须设置此项：否者窗口关闭后将不能通过托盘显示
		Platform.setImplicitExit(false);
	}
	
	/**
	 * 是否支持托盘
	 */
	private final boolean support;
	/**
	 * 托盘容器
	 */
	private Stage trayStage;
	/**
	 * 托盘
	 */
	private TrayIcon trayIcon;
	
	private TrayMenu() {
		this.support = SystemTray.isSupported();
		if(this.support) {
			init();
			initMenu();
			enableTray();
		}
	}
	
	public static final TrayMenu getInstance() {
		return INSTANCE;
	}

	private MenuItem showMenu;
	private MenuItem hideMenu;
	private MenuItem exitMenu;
	private MenuItem aboutMenu;
	private MenuItem sourceMenu;
	private MenuItem supportMenu;

	@Override
	protected void initMenu() {
		this.showMenu = buildMenuItem("显示", "/image/16/show.png");
		this.hideMenu = buildMenuItem("隐藏", "/image/16/hide.png");
		this.sourceMenu = buildMenuItem("官网与源码", "/image/16/source.png");
		this.supportMenu = buildMenuItem("问题与建议", "/image/16/support.png");
		this.aboutMenu = buildMenuItem("关于", "/image/16/about.png");
		this.exitMenu = buildMenuItem("退出", "/image/16/exit.png");
		
		this.showMenu.setOnAction(this.showAction);
		this.hideMenu.setOnAction(this.hideAction);
		this.exitMenu.setOnAction(this.exitAction);
		this.aboutMenu.setOnAction(this.aboutAction);
		this.sourceMenu.setOnAction(this.sourceAction);
		this.supportMenu.setOnAction(this.supportAction);
		
		addMenu(this.showMenu);
		addMenu(this.hideMenu);
		addMenu(this.sourceMenu);
		addMenu(this.supportMenu);
		addMenu(this.aboutMenu);
		this.addSeparator();
		addMenu(this.exitMenu);
		
		this.addEventFilter(WindowEvent.WINDOW_HIDDEN, this.windowHiddenAction);
	}
	
	/**
	 * 添加系统托盘
	 */
	private void enableTray() {
		final MouseListener mouseListener = new MouseInputAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent event) {
				// 左键：显示隐藏
				if (event.getButton() == java.awt.event.MouseEvent.BUTTON1) {
					if (MainWindow.getInstance().isShowing()) {
						Platform.runLater(() -> {
							MainWindow.getInstance().hide();
						});
					} else {
						Platform.runLater(() -> {
							MainWindow.getInstance().show();
						});
					}
				} else if(event.getButton() == java.awt.event.MouseEvent.BUTTON3) {
					// 右键：托盘菜单
					Platform.runLater(() -> {
						final int x = event.getXOnScreen();
						final int y = event.getYOnScreen() - MENU_WINDOW_HEIGHT;
						TrayMenu.INSTANCE.show(createTrayStage(), x, y);
					});
				}
			}
		};
		try {
			final BufferedImage image = ImageIO.read(MainWindow.class.getResourceAsStream("/image/16/logo.png"));
			this.trayIcon = new TrayIcon(image, SystemConfig.getName());
			this.trayIcon.addMouseListener(mouseListener);
			SystemTray.getSystemTray().add(this.trayIcon);
		} catch (IOException | AWTException e) {
			LOGGER.error("添加系统托盘异常", e);
		}
	}
	
	/**
	 * 提示信息（提示）
	 */
	public void info(String title, String content) {
		notice(title, content, MessageType.INFO);
	}
	
	/**
	 * 提示信息（警告）
	 */
	public void warn(String title, String content) {
		notice(title, content, MessageType.WARNING);
	}

	/**
	 * 提示信息
	 */
	public void notice(String title, String content, MessageType type) {
		if(DownloadConfig.getNotice() && this.support) {
			this.trayIcon.displayMessage(title, content, type);
		}
	}
	
	/**
	 * 关闭托盘
	 */
	public static final void exit() {
		if(TrayMenu.getInstance().support) {
			final TrayIcon trayIcon = TrayMenu.getInstance().trayIcon;
			SystemTray.getSystemTray().remove(trayIcon);
		}
	}
	
	/**
	 * 创建托盘菜单容器
	 */
	private Stage createTrayStage() {
		final FlowPane trayPane = new FlowPane();
		trayPane.setBackground(Background.EMPTY);
		final Scene trayScene = new Scene(trayPane);
		trayScene.setFill(Color.TRANSPARENT);
		final Stage trayStage = new Stage();
		trayStage.initStyle(StageStyle.UTILITY);
		trayStage.setOpacity(0);
		trayStage.setMaxWidth(0);
		trayStage.setMaxHeight(0);
		trayStage.setAlwaysOnTop(true);
		trayStage.setScene(trayScene);
		trayStage.show();
		this.trayStage = trayStage;
		return trayStage;
	}
	
	/**
	 * 显示
	 */
	private EventHandler<ActionEvent> showAction = (event) -> {
		Platform.runLater(() -> {
			MainWindow.getInstance().show();
		});
	};
	
	/**
	 * 隐藏
	 */
	private EventHandler<ActionEvent> hideAction = (event) -> {
		Platform.runLater(() -> {
			MainWindow.getInstance().hide();
		});
	};
	
	/**
	 * 退出
	 */
	private EventHandler<ActionEvent> exitAction = (event) -> {
		SystemContext.shutdown();
	};
	
	/**
	 * 关于
	 */
	private EventHandler<ActionEvent> aboutAction = (event) -> {
		AboutWindow.getInstance().show();
	};
	
	/**
	 * 官网与源码
	 */
	private EventHandler<ActionEvent> sourceAction = (event) -> {
		BrowseUtils.open(SystemConfig.getSource());
	};
	
	/**
	 * 问题与建议
	 */
	private EventHandler<ActionEvent> supportAction = (event) -> {
		BrowseUtils.open(SystemConfig.getSupport());
	};
	
	/**
	 * 窗口隐藏时移除托盘菜单的容器
	 */
	private EventHandler<WindowEvent> windowHiddenAction = (event) -> {
		Platform.runLater(() -> {
			if(this.trayStage != null) {
				this.trayStage.close();
				this.trayStage = null;
			}
		});
	};
	
}
