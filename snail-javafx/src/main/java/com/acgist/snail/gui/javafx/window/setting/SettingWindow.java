package com.acgist.snail.gui.javafx.window.setting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.javafx.window.AbstractWindow;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>设置窗口</p>
 * 
 * @author acgist
 */
public final class SettingWindow extends AbstractWindow<SettingController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SettingWindow.class);
	
	private static final SettingWindow INSTANCE;
	
	public static final SettingWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		LOGGER.debug("初始化设置窗口");
		INSTANCE = new SettingWindow();
	}
	
	private SettingWindow() {
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, "设置", 600, 400, "/fxml/setting.fxml", Modality.APPLICATION_MODAL);
		this.dialogWindow();
	}
	
}