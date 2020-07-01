package com.acgist.snail.gui.event.adapter;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.GuiManager.Mode;
import com.acgist.snail.gui.GuiManager.SnailNoticeType;
import com.acgist.snail.gui.event.GuiEventExtend;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.system.format.BEncodeEncoder;

/**
 * <p>GUI提示消息事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class NoticeEventAdapter extends GuiEventExtend {

	private static final Logger LOGGER = LoggerFactory.getLogger(NoticeEventAdapter.class);
	
	protected NoticeEventAdapter() {
		super(Type.NOTICE, "提示消息事件");
	}
	
	@Override
	protected void executeExtend(GuiManager.Mode mode, Object ... args) {
		SnailNoticeType type;
		String title;
		String message;
		if(args == null) {
			LOGGER.warn("提示消息错误（参数错误）：{}", args);
			return;
		} else if(args.length == 2) {
			title = (String) args[0];
			message = (String) args[1];
			type = SnailNoticeType.INFO;
		} else if(args.length == 3) {
			title = (String) args[0];
			message = (String) args[1];
			type = (SnailNoticeType) args[2];
		} else {
			LOGGER.warn("提示消息错误（参数长度错误）：{}", args.length);
			return;
		}
		if(mode == Mode.NATIVE) {
			this.executeNativeExtend(type, title, message);
		} else {
			this.executeExtendExtend(type, title, message);
		}
	}
	
	/**
	 * <p>本地提示消息</p>
	 * 
	 * @param type 类型
	 * @param title 标题
	 * @param message 消息
	 */
	protected abstract void executeNativeExtend(SnailNoticeType type, String title, String message);
	
	/**
	 * <p>扩展提示消息</p>
	 * 
	 * @param type 类型
	 * @param title 标题
	 * @param message 消息
	 */
	private final void executeExtendExtend(SnailNoticeType type, String title, String message) {
		final ApplicationMessage applicationMessage = ApplicationMessage.message(ApplicationMessage.Type.NOTICE);
		final Map<String, String> map = new HashMap<>(5);
		map.put("type", type.name());
		map.put("title", title);
		map.put("message", message);
		final String body = BEncodeEncoder.encodeMapString(map);
		applicationMessage.setBody(body);
		GuiManager.getInstance().sendExtendGuiMessage(applicationMessage);
	}

}
