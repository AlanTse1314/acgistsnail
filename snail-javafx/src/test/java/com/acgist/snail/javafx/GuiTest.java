package com.acgist.snail.javafx;

import org.junit.Test;

import com.acgist.snail.gui.GuiManager.MessageType;
import com.acgist.snail.gui.javafx.Alerts;

import javafx.application.Platform;

public class GuiTest extends BaseTest {

	@Test
	public void testAlert() {
		Platform.startup(() -> {});
		Platform.runLater(() -> {
			Alerts.info("测试", "测试信息");
			Alerts.warn("警告", "警告信息");
			Alerts.build("确认", "是否删除？", MessageType.CONFIRM);
		});
		this.pause();
	}
	
}
