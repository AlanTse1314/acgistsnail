package com.acgist.snail.net.ftp;

import java.io.InputStream;
import java.net.URI;

import com.acgist.snail.net.client.ftp.FtpClient;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * FTP工具
 */
public class FtpManager {

	private static final int FTP_DEFAULT_PORT = 21;
	
	private String url; // 下载链接
	private String host;
	private int port;
	private String filePath;
	private String user;
	private String password;
	
	private FtpClient client;

	public FtpManager(String url) {
		this.url = url;
		decodeUrl();
		client = new FtpClient(host, port, user, password, filePath);
		client.connect();
	}
	
	/**
	 * 获取文件大小
	 */
	public Long size() throws NetException {
		return client.size();
	}
	
	/**
	 * 下载文件
	 */
	public InputStream download() {
		return client.download();
	}

	/**
	 * 关闭
	 */
	public void close() {
		client.close();
	}
	
	/**
	 * 解码：获取信息
	 */
	private void decodeUrl() {
		URI uri= URI.create(this.url);
		String userInfo = uri.getUserInfo();
		setUserInfo(userInfo);
		this.filePath = uri.getPath();
		this.host = uri.getHost();
		int port = uri.getPort();
		if(port == -1) {
			port = FTP_DEFAULT_PORT;
		}
		this.port = port;
	}

	/**
	 * 设置用户信息
	 */
	private void setUserInfo(String userInfo) {
		if(StringUtils.isEmpty(userInfo)) {
			this.user = FtpClient.ANONYMOUS;
			this.password = FtpClient.ANONYMOUS;
		} else {
			String[] userInfos = userInfo.split(":");
			if(userInfos.length == 1) {
				this.user = userInfos[0];
			} else if(userInfos.length == 2) {
				this.user = userInfos[0];
				this.password = userInfos[1];
			} else {
				this.user = FtpClient.ANONYMOUS;
				this.password = FtpClient.ANONYMOUS;
			}
		}
	}
	
}
