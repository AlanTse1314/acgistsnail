package com.acgist.snail.net.ftp.bootstrap;

import java.net.URI;

import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>FtpClient Builder</p>
 * <p>使用FTP链接创建FTP客户端</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class FtpClientBuilder {

	/**
	 * <p>FTP默认端口</p>
	 */
	private static final int DEFAULT_PORT = 21;
	
	/**
	 * <p>下载链接</p>
	 */
	private final String url;
	/**
	 * <p>服务器地址</p>
	 */
	private String host;
	/**
	 * <p>服务器端口</p>
	 */
	private int port;
	/**
	 * <p>用户账号</p>
	 */
	private String user;
	/**
	 * <p>用户密码</p>
	 */
	private String password;
	/**
	 * <p>文件路径</p>
	 */
	private String filePath;
	
	private FtpClientBuilder(String url) {
		this.url = url;
	}

	public static final FtpClientBuilder newInstance(String url) {
		return new FtpClientBuilder(url);
	}
	
	/**
	 * <p>创建FtpClient</p>
	 */
	public FtpClient build() {
		this.decodeUrl();
		return FtpClient.newInstance(
			this.host,
			this.port,
			this.user,
			this.password,
			this.filePath
		);
	}
	
	/**
	 * <p>解析URL：地址、端口、用户、文件等信息</p>
	 */
	private void decodeUrl() {
		final URI uri = URI.create(this.url);
		final String userInfo = uri.getUserInfo();
		decodeUserInfo(userInfo);
		this.host = uri.getHost();
		int port = uri.getPort();
		if(port == -1) {
			port = DEFAULT_PORT;
		}
		this.port = port;
		this.filePath = uri.getPath();
	}

	/**
	 * <p>解析用户授权信息</p>
	 */
	private void decodeUserInfo(String userInfo) {
		if(StringUtils.isEmpty(userInfo)) {
			this.user = SystemConfig.getFtpUser();
			this.password = SystemConfig.getFtpPassword();
		} else {
			final String[] userInfos = userInfo.split(":");
			if(userInfos.length == 1) {
				this.user = userInfos[0];
				this.password = SystemConfig.getFtpPassword();
			} else if(userInfos.length == 2) {
				this.user = userInfos[0];
				this.password = userInfos[1];
			} else {
				this.user = SystemConfig.getFtpUser();
				this.password = SystemConfig.getFtpPassword();
			}
		}
	}

}
