package com.acgist.snail.gui.event.adapter;

import com.acgist.snail.context.GuiContext;
import com.acgist.snail.gui.event.GuiEvent;

/**
 * <p>GUI创建窗口事件</p>
 * 
 * @author acgist
 */
public class BuildEventAdapter extends GuiEvent {
	
	public BuildEventAdapter() {
		super(Type.BUILD, "创建窗口事件");
	}
	
	@Override
	protected void executeNative(Object ... args) {
		this.executeExtend(args);
	}

	@Override
	protected void executeExtend(Object ... args) {
		GuiContext.getInstance().lock();
	}
	
}
