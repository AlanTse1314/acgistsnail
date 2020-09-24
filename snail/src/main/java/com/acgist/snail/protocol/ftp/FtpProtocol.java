package com.acgist.snail.protocol.ftp;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.ftp.FtpDownloader;
import com.acgist.snail.exception.DownloadException;
import com.acgist.snail.exception.NetException;
import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.net.ftp.bootstrap.FtpClientBuilder;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.protocol.Protocol;

/**
 * <p>FTP协议</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class FtpProtocol extends Protocol {
	
	private static final FtpProtocol INSTANCE = new FtpProtocol();
	
	public static final FtpProtocol getInstance() {
		return INSTANCE;
	}
	
	private FtpProtocol() {
		super(Type.FTP);
	}

	@Override
	public String name() {
		return "FTP";
	}
	
	@Override
	public boolean available() {
		return true;
	}
	
	@Override
	public IDownloader buildDownloader(ITaskSession taskSession) {
		return FtpDownloader.newInstance(taskSession);
	}

	@Override
	protected void buildSize() throws DownloadException {
		final FtpClient client = FtpClientBuilder.newInstance(this.url).build();
		try {
			client.connect();
			final long size = client.size();
			this.taskEntity.setSize(size);
		} catch (NetException e) {
			throw new DownloadException(e);
		} finally {
			client.close();
		}
	}

}
