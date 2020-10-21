package com.acgist.snail.gui.event.adapter;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.pojo.message.ApplicationMessage;

/**
 * <p>GUI显示窗口事件</p>
 * 
 * @author acgist
 */
public class ShowEventAdapter extends GuiEvent {

	protected ShowEventAdapter() {
		super(Type.SHOW, "显示窗口事件");
	}
	
	@Override
	protected void executeNative(Object... args) {
		this.executeExtend(args);
	}

	@Override
	protected void executeExtend(Object ... args) {
		final ApplicationMessage message = ApplicationMessage.message(ApplicationMessage.Type.SHOW);
		GuiManager.getInstance().sendExtendGuiMessage(message);
	}

}
