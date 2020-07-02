package com.acgist.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.acgist.gui.extend.ExtendGuiManager;
import com.acgist.snail.net.application.ApplicationClient;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.pojo.message.ApplicationMessage.Type;
import com.acgist.snail.system.format.BEncodeEncoder;

/**
 * <p>Snail启动类</p>
 * <p>使用系统消息与后台模式启动的Snail（蜗牛）进行通信和下载</p>
 * <p>后台模式启动：`snail-javafx`</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
public final class Application {

	/**
	 * <p>启动方法</p>
	 * 
	 * @param args 启动参数
	 */
	public static final void main(String[] args) {
		String message = null;
		final Scanner scanner = new Scanner(System.in);
		ExtendGuiManager.getInstance().registerEvent();
		final ApplicationClient client = ApplicationClient.newInstance();
		client.connect();
		while ((message = scanner.nextLine()) != null) {
			if(message.equalsIgnoreCase(Type.CLOSE.name())) {
				client.send(ApplicationMessage.message(Type.CLOSE, message));
				client.close();
				break;
			} else if(message.equalsIgnoreCase(Type.SHUTDOWN.name())) {
				client.send(ApplicationMessage.message(Type.SHUTDOWN, message));
				client.close();
				break;
			} else if(message.equalsIgnoreCase(Type.GUI.name())) {
				// 注册外部GUI
				client.send(ApplicationMessage.message(Type.GUI, message));
			} else if(message.equalsIgnoreCase(Type.NOTIFY.name())) {
				client.send(ApplicationMessage.message(Type.NOTIFY, message));
			} else if(message.equalsIgnoreCase(Type.TASK_NEW.name())) {
				final Map<String, String> map = new HashMap<>();
				// 单个文件任务
				map.put("url", "https://mirror.bit.edu.cn/apache/tomcat/tomcat-9/v9.0.36/bin/apache-tomcat-9.0.36.zip");
				// BT任务
//				map.put("url", "E:\\snail\\0000.torrent");
//				map.put("files", "l50:[UHA-WINGS][Vinland Saga][01][x264 1080p][CHT].mp4e");
				client.send(ApplicationMessage.message(Type.TASK_NEW, BEncodeEncoder.encodeMapString(map)));
			} else if(message.equalsIgnoreCase(Type.TASK_LIST.name())) {
				client.send(ApplicationMessage.message(Type.TASK_LIST, message));
			} else if(message.equalsIgnoreCase(Type.TASK_START.name())) {
				client.send(ApplicationMessage.message(Type.TASK_START, "37f48162-d306-4fff-b161-f1231a3f7e48"));
			} else if(message.equalsIgnoreCase(Type.TASK_PAUSE.name())) {
				client.send(ApplicationMessage.message(Type.TASK_PAUSE, "37f48162-d306-4fff-b161-f1231a3f7e48"));
			} else if(message.equalsIgnoreCase(Type.TASK_DELETE.name())) {
				client.send(ApplicationMessage.message(Type.TASK_DELETE, "37f48162-d306-4fff-b161-f1231a3f7e48"));
			} else {
				client.send(ApplicationMessage.text(message));
			}
		}
		scanner.close();
	}

}