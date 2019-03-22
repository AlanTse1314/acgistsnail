package com.acgist.snail.net.application;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.ATcpClient;
import com.acgist.snail.net.message.impl.ClientMessageHandler;
import com.acgist.snail.pojo.message.ClientMessage;
import com.acgist.snail.pojo.message.ClientMessage.Type;
import com.acgist.snail.system.config.SystemConfig;

/**
 * 启动检测：如果已经启动实例，通过这个方法唤醒已启动的窗口
 */
public class ApplicationClient extends ATcpClient<ClientMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationClient.class);
	
	private ApplicationClient() {
		super(ClientMessageHandler.SPLIT, new ClientMessageHandler());
	}
	
	public static final ApplicationClient newInstance() {
		return new ApplicationClient();
	}
	
	@Override
	public boolean connect() {
		return connect(SystemConfig.getServerHost(), SystemConfig.getServerPort());
	}
	
	/**
	 * 发送客户端消息
	 */
	private void send(ClientMessage message) {
		send(message.toJson());
	}
	
	/**
	 * 用户输入传输
	 */
	private void readin() {
		Scanner scanner = new Scanner(System.in);
		String message = null;
		while ((message = scanner.next()) != null) {
			if(message.equals("close")) {
				send(ClientMessage.message(Type.close, message));
				close();
				break;
			} else {
				send(ClientMessage.text(message));
			}
		}
		scanner.close();
	}
	
	/**
	 * 唤起主窗口
	 */
	public static final void notifyWindow() {
		ApplicationClient client = ApplicationClient.newInstance();
		try {
			client.connect();
			client.send(ClientMessage.message(ClientMessage.Type.notify));
			client.send(ClientMessage.message(ClientMessage.Type.close));
		} catch (Exception e) {
			LOGGER.error("通知主窗口异常", e);
		} finally {
			client.close();
		}
	}
	
	public static final void main(String[] args) {
		ApplicationClient client = ApplicationClient.newInstance();
		client.connect();
		client.readin();
	}

}
