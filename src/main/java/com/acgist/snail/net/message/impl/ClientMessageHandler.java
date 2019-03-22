package com.acgist.snail.net.message.impl;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.main.MainWindow;
import com.acgist.snail.net.message.TcpMessageHandler;
import com.acgist.snail.pojo.message.ClientMessage;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.StringUtils;

import javafx.application.Platform;

/**
 * 客户端消息
 */
public class ClientMessageHandler extends TcpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientMessageHandler.class);
	
	public static final String SPLIT = "\r\n"; // 处理粘包分隔符
	
	private StringBuffer contentBuffer = new StringBuffer();
	
	public ClientMessageHandler() {
		super(SPLIT);
	}
	
	@Override
	public boolean doMessage(Integer result, ByteBuffer attachment) {
		boolean doNext = true; // 是否继续处理消息
		if (result == 0) {
			LOGGER.info("读取空消息");
		} else {
			String content = IoUtils.readContent(attachment);
			if(content.contains(SPLIT)) {
				int index = content.indexOf(SPLIT);
				while(index >= 0) {
					contentBuffer.append(content.substring(0, index));
					doNext = oneMessage(contentBuffer.toString());
					contentBuffer.setLength(0);
					content = content.substring(index + SPLIT.length());
					index = content.indexOf(SPLIT);
				}
			}
			contentBuffer.append(content);
		}
		return doNext;
	};
	
	/**
	 * 单挑消息处理
	 */
	private boolean oneMessage(String json) {
		json = json.trim();
		if(StringUtils.isEmpty(json)) {
			LOGGER.warn("读取消息内容为空");
			return true;
		}
		ClientMessage message = ClientMessage.valueOf(json);
		if(message == null) {
			LOGGER.warn("读取消息格式错误：{}", json);
			return true;
		}
		LOGGER.info("读取消息：{}", json);
		return !this.execute(message);
	}
	
	/**
	 * 处理消息
	 * @return 是否关闭socket：true-关闭；false-继续
	 */
	private boolean execute(ClientMessage message) {
		boolean close = false; // 是否关闭
		if(message.getType() == ClientMessage.Type.text) { // 文本信息：直接原样返回
			send(ClientMessage.response(message.getBody()));
		} else if(message.getType() == ClientMessage.Type.notify) { // 唤醒主窗口
			Platform.runLater(() -> {
				MainWindow.getInstance().show();
			});
		} else if(message.getType() == ClientMessage.Type.close) { // 关闭
			close = true;
			IoUtils.close(socket);
		} else if(message.getType() == ClientMessage.Type.response) { // 响应内容
			LOGGER.info("收到响应：{}", message.getBody());
		} else {
			LOGGER.warn("未适配的消息类型：{}", message.getType());
		}
		return close;
	}

	/**
	 * 发送消息
	 */
	private void send(ClientMessage message) {
		send(message.toJson());
	}

}
