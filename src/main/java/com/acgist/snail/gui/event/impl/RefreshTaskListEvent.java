package com.acgist.snail.gui.event.impl;

import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.main.TaskDisplay;
import com.acgist.snail.pojo.message.ApplicationMessage;

/**
 * GUI刷新任务列表事件
 * 
 * @author acgist
 * @since 1.1.0
 */
public class RefreshTaskListEvent extends GuiEvent {

	private static final RefreshTaskListEvent INSTANCE = new RefreshTaskListEvent();
	
	protected RefreshTaskListEvent() {
		super(Type.refreshTaskList, "刷新任务列表事件");
	}

	@Override
	protected void executeNative(Object ... args) {
		TaskDisplay.getInstance().refreshTaskList();
	}

	@Override
	protected void executeExtend(Object ... args) {
		final ApplicationMessage message = ApplicationMessage.message(ApplicationMessage.Type.refresh);
		GuiHandler.getInstance().sendGuiMessage(message);
	}

	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
}
