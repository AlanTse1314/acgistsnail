package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.protocol.ftp.FtpProtocol;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.protocol.thunder.ThunderProtocol;
import com.acgist.snail.protocol.torrent.TorrentProtocol;
import com.acgist.snail.system.initializer.Initializer;
import com.acgist.snail.system.manager.ProtocolManager;

/**
 * <p>初始化下载协议</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ProtocolInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolInitializer.class);
	
	private ProtocolInitializer() {
	}
	
	public static final ProtocolInitializer newInstance() {
		return new ProtocolInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化下载协议");
		ProtocolManager.getInstance()
			.register(FtpProtocol.getInstance())
			.register(HttpProtocol.getInstance())
			.register(MagnetProtocol.getInstance())
			.register(TorrentProtocol.getInstance())
			.register(ThunderProtocol.getInstance())
			.available(true);
	}

}
