package com.acgist.snail.gui.event.impl;

import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.menu.TrayMenu;

import javafx.application.Platform;

/**
 * GUI退出窗口事件
 * 
 * @author acgist
 * @since 1.1.0
 */
public class ExitEvent extends GuiEvent {

	private static final ExitEvent INSTANCE = new ExitEvent();
	
	protected ExitEvent() {
		super(Type.exit, "退出窗口事件");
	}
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}

	@Override
	protected void executeNative(Object ... args) {
		Platform.exit(); // 退出平台
		TrayMenu.exit(); // 退出托盘
	}

	@Override
	protected void executeExtend(Object ... args) {
		GuiHandler.getInstance().unlock(); // 唤醒扩展GUI等待锁
	}
	
}
