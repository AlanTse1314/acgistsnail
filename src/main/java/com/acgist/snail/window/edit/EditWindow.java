package com.acgist.snail.window.edit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.window.AbstractWindow;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * 编辑任务窗口
 */
public class EditWindow extends AbstractWindow<EditController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(EditWindow.class);
	
	private static EditWindow INSTANCE;
	
	private EditWindow() {
	}

	public static final EditWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		synchronized (EditWindow.class) {
			if(INSTANCE == null) {
				LOGGER.info("初始化编辑任务窗口");
				INSTANCE = new EditWindow();
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
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/EditPane.fxml"));
		FlowPane root = loader.load();
		this.controller = loader.getController();
		Scene scene = new Scene(root, 800, 600);
		stage.setScene(scene);
		stage.setTitle("编辑任务");
		disableResize();
		dialogWindow();
	}
	
}