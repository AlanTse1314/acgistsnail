package com.acgist.gui.extend.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiManager.SnailNoticeType;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.NoticeEventAdapter;

/**
 * <p>GUI提示消息事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class NoticeEvent extends NoticeEventAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NoticeEvent.class);
	
	private static final NoticeEvent INSTANCE = new NoticeEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	@Override
	protected void executeExtendExtend(SnailNoticeType type, String title, String message) {
		LOGGER.debug("收到提示消息信息：{}-{}-{}", type, title, message);
	}
	
}
