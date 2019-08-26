package com.acgist.snail.gui.setting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Window;

import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 设置窗口
 * 
 * @author acgist
 * @since 1.0.0
 */
public class SettingWindow extends Window<SettingController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SettingWindow.class);
	
	private static SettingWindow INSTANCE;
	
	private SettingWindow() {
	}

	public static final SettingWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		synchronized (SettingWindow.class) {
			if(INSTANCE == null) {
				LOGGER.debug("初始化设置窗口");
				INSTANCE = new SettingWindow();
				try {
					INSTANCE.start(INSTANCE.stage);
				} catch (Exception e) {
					LOGGER.error("窗口初始化异常", e);
				}
			}
		}
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		final FlowPane root = super.loadFxml("/fxml/setting.fxml");
		final Scene scene = new Scene(root, 600, 600);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.setTitle("设置");
		disableResize();
		dialogWindow();
	}
	
}