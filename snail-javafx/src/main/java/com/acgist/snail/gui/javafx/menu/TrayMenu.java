package com.acgist.snail.gui.javafx.menu;

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

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemContext;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.javafx.Controller;
import com.acgist.snail.gui.javafx.Fonts.SnailIcon;
import com.acgist.snail.gui.javafx.Menu;
import com.acgist.snail.gui.javafx.Window;
import com.acgist.snail.gui.javafx.about.AboutWindow;
import com.acgist.snail.gui.javafx.main.MainWindow;
import com.acgist.snail.gui.utils.DesktopUtils;

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
 * <p>托盘菜单</p>
 * <p>系统托盘使用AWT实现</p>
 * <pre>
 * // 不能直接在AWT线程中调用JavaFX线程，需要转换：
 * Platform.runLater(() -&gt; {});
 * </pre>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TrayMenu extends Menu {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrayMenu.class);
	
	private static final TrayMenu INSTANCE;
	
	public static final TrayMenu getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>窗口高度</p>
	 */
	private static final int MENU_WINDOW_HEIGHT = 100;
	
	static {
		LOGGER.debug("初始化托盘菜单");
		INSTANCE = new TrayMenu();
		// 必须设置此项：否者窗口关闭后将不能通过托盘显示
		Platform.setImplicitExit(false);
	}
	
	/**
	 * <p>是否支持托盘</p>
	 */
	private final boolean support;
	/**
	 * <p>托盘容器</p>
	 */
	private Stage trayStage;
	/**
	 * <p>托盘</p>
	 */
	private TrayIcon trayIcon;
	/**
	 * <p>显示按钮</p>
	 */
	private MenuItem showMenu;
	/**
	 * <p>隐藏按钮</p>
	 */
	private MenuItem hideMenu;
	/**
	 * <p>官网与源码按钮</p>
	 */
	private MenuItem sourceMenu;
	/**
	 * <p>问题与建议按钮</p>
	 */
	private MenuItem supportMenu;
	/**
	 * <p>关于按钮</p>
	 */
	private MenuItem aboutMenu;
	/**
	 * <p>退出按钮</p>
	 */
	private MenuItem exitMenu;

	private TrayMenu() {
		this.support = SystemTray.isSupported();
		if(this.support) {
			this.initMenu();
			this.enableTray();
		}
	}
	
	@Override
	protected void initMenu() {
		// 创建按钮
		this.showMenu = buildMenuItem("显示", SnailIcon.AS_ENLARGE);
		this.hideMenu = buildMenuItem("隐藏", SnailIcon.AS_SHRINK);
		this.sourceMenu = buildMenuItem("官网与源码", SnailIcon.AS_HOME2);
		this.supportMenu = buildMenuItem("问题与建议", SnailIcon.AS_ROCKET);
		this.aboutMenu = buildMenuItem("关于", SnailIcon.AS_INFO);
		this.exitMenu = buildMenuItem("退出", SnailIcon.AS_SWITCH);
		// 设置按钮事件
		this.showMenu.setOnAction(this.showAction);
		this.hideMenu.setOnAction(this.hideAction);
		this.sourceMenu.setOnAction(this.sourceAction);
		this.supportMenu.setOnAction(this.supportAction);
		this.aboutMenu.setOnAction(this.aboutAction);
		this.exitMenu.setOnAction(this.exitAction);
		// 添加按钮
		this.addMenu(this.showMenu);
		this.addMenu(this.hideMenu);
		this.addMenu(this.sourceMenu);
		this.addMenu(this.supportMenu);
		this.addMenu(this.aboutMenu);
		this.addSeparator();
		this.addMenu(this.exitMenu);
		// 设置窗口隐藏事件
		this.addEventFilter(WindowEvent.WINDOW_HIDDEN, this.windowHiddenAction);
	}
	
	/**
	 * <p>添加系统托盘</p>
	 */
	private void enableTray() {
		// 托盘鼠标事件
		final MouseListener mouseListener = new MouseInputAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent event) {
				if (event.getButton() == java.awt.event.MouseEvent.BUTTON1) { // 左键：显示隐藏
					if (MainWindow.getInstance().isShowing()) {
						Platform.runLater(() -> {
							MainWindow.getInstance().hide();
						});
					} else {
						Platform.runLater(() -> {
							MainWindow.getInstance().show();
						});
					}
				} else if(event.getButton() == java.awt.event.MouseEvent.BUTTON3) { // 右键：托盘菜单
					// 显示托盘菜单
					Platform.runLater(() -> {
						final int x = event.getXOnScreen();
						final int y = event.getYOnScreen() - MENU_WINDOW_HEIGHT;
						TrayMenu.INSTANCE.show(createTrayStage(), x, y);
					});
				}
			}
		};
		// 添加系统托盘
		try(final var input = MainWindow.class.getResourceAsStream(Controller.LOGO_ICON_16)) {
			final BufferedImage image = ImageIO.read(input);
			this.trayIcon = new TrayIcon(image, SystemConfig.getName());
			this.trayIcon.addMouseListener(mouseListener);
			SystemTray.getSystemTray().add(this.trayIcon);
		} catch (IOException | AWTException e) {
			LOGGER.error("添加系统托盘异常", e);
		}
	}
	
	/**
	 * <p>提示信息（提示）</p>
	 * 
	 * @param title 标题
	 * @param content 内容
	 */
	public void info(String title, String content) {
		this.notice(title, content, GuiManager.MessageType.INFO);
	}
	
	/**
	 * <p>提示信息（警告）</p>
	 * 
	 * @param title 标题
	 * @param content 内容
	 */
	public void warn(String title, String content) {
		this.notice(title, content, GuiManager.MessageType.WARN);
	}

	/**
	 * <p>提示信息</p>
	 * 
	 * @param title 标题
	 * @param content 内容
	 * @param type 类型
	 */
	public void notice(String title, String content, GuiManager.MessageType type) {
		if(DownloadConfig.getNotice() && this.support) {
			this.trayIcon.displayMessage(title, content, this.getMessageType(type));
		}
	}
	
	/**
	 * <p>关闭托盘</p>
	 */
	public static final void exit() {
		if(TrayMenu.getInstance().support) {
			final TrayIcon trayIcon = TrayMenu.getInstance().trayIcon;
			SystemTray.getSystemTray().remove(trayIcon);
		}
	}
	
	/**
	 * <p>创建托盘菜单容器</p>
	 * 
	 * @return 容器
	 */
	private Stage createTrayStage() {
		final FlowPane trayPane = new FlowPane();
		trayPane.setBackground(Background.EMPTY);
		trayPane.getStyleClass().add("tray"); // 添加托盘样式
		final Scene trayScene = new Scene(trayPane);
		Window.applyTheme(trayScene);
		trayScene.setFill(Color.TRANSPARENT); // 隐藏托盘容器
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
	 * <p>显示</p>
	 */
	private EventHandler<ActionEvent> showAction = event -> {
		Platform.runLater(() -> {
			MainWindow.getInstance().show();
		});
	};
	
	/**
	 * <p>隐藏</p>
	 */
	private EventHandler<ActionEvent> hideAction = event -> {
		Platform.runLater(() -> {
			MainWindow.getInstance().hide();
		});
	};
	
	/**
	 * <p>退出</p>
	 */
	private EventHandler<ActionEvent> exitAction = event -> {
		SystemContext.shutdown();
	};
	
	/**
	 * <p>关于</p>
	 */
	private EventHandler<ActionEvent> aboutAction = event -> {
		AboutWindow.getInstance().show();
	};
	
	/**
	 * <p>官网与源码</p>
	 */
	private EventHandler<ActionEvent> sourceAction = event -> {
		DesktopUtils.browse(SystemConfig.getSource());
	};
	
	/**
	 * <p>问题与建议</p>
	 */
	private EventHandler<ActionEvent> supportAction = event -> {
		DesktopUtils.browse(SystemConfig.getSupport());
	};
	
	/**
	 * <p>窗口隐藏时：移除托盘菜单容器</p>
	 */
	private EventHandler<WindowEvent> windowHiddenAction = event -> {
		Platform.runLater(() -> {
			if(this.trayStage != null) {
				this.trayStage.close();
				this.trayStage = null;
			}
		});
	};
	
	
	/**
	 * <p>获取JavaFX消息类型</p>
	 * 
	 * @return JavaFX消息类型
	 */
	public final MessageType getMessageType(GuiManager.MessageType type) {
		switch (type) {
		case NONE:
			return MessageType.NONE;
		case INFO:
			return MessageType.INFO;
		case WARN:
			return MessageType.WARNING;
		case ERROR:
			return MessageType.ERROR;
		default:
			return MessageType.INFO;
		}
	}
	
}
