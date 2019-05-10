package com.acgist.snail.net.application;

import com.acgist.snail.net.TcpServer;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.ThreadUtils;

/**
 * 系统监听
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ApplicationServer extends TcpServer {

	private static final ApplicationServer INSTANCE = new ApplicationServer();
	
	private ApplicationServer() {
		super("Application Server");
	}
	
	public static final ApplicationServer getInstance() {
		return INSTANCE;
	}
	
	public boolean listen() {
		return listen(SystemConfig.getServerPort());
	}
	
	public boolean listen(String host, int port) {
		return listen(host, port, ApplicationMessageHandler.class);
	}
	
	public static final void main(String[] args) {
		ApplicationServer.getInstance().listen();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}

}