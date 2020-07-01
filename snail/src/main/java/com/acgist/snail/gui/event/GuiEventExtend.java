package com.acgist.snail.gui.event;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.GuiManager.Mode;

/**
 * <p>GUI事件扩展</p>
 * <p>处理变长参数GUI事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class GuiEventExtend extends GuiEvent {

	protected GuiEventExtend(Type type, String name) {
		super(type, name);
	}
	
	@Override
	protected final void executeNative(Object ... args) {
		this.executeExtend(Mode.NATIVE, args);
	}

	@Override
	protected final void executeExtend(Object ... args) {
		this.executeExtend(Mode.EXTEND, args);
	}
	
	/**
	 * <p>执行变长参数GUI事件</p>
	 * 
	 * @param mode 运行模式
	 * @param args 变长参数
	 */
	protected abstract void executeExtend(GuiManager.Mode mode, Object ... args);

}
