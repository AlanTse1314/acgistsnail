package com.acgist.snail.net.torrent.tracker;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.utils.PeerUtils;

/**
 * <p>UDP Tracker消息代理</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TrackerMessageHandler extends UdpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerMessageHandler.class);

	/**
	 * Announce消息最小字节长度
	 */
	private static final int ANNOUNCE_MIN_SIZE = 20;
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) {
		buffer.flip();
		final int action = buffer.getInt();
		if (action == TrackerConfig.Action.connect.action()) {
			doConnect(buffer);
		} else if(action == TrackerConfig.Action.announce.action()) {
			doAnnounce(buffer);
		} else if(action == TrackerConfig.Action.scrape.action()) {
			// 刮檫
		} else if(action == TrackerConfig.Action.error.action()) {
			LOGGER.warn("Tracker错误");
		}
		buffer.clear();
	}

	/**
	 * 处理连接
	 */
	private void doConnect(ByteBuffer buffer) {
		final int trackerId = buffer.getInt();
		final long connectionId = buffer.getLong();
		TrackerManager.getInstance().connectionId(trackerId, connectionId);
	}

	/**
	 * 处理Peer
	 */
	private void doAnnounce(ByteBuffer buffer) {
		// 消息长度
		final int size = buffer.limit();
		if(size < ANNOUNCE_MIN_SIZE) {
			LOGGER.debug("Announce消息长度错误：{}", size);
			return;
		}
		final AnnounceMessage message = new AnnounceMessage();
		message.setId(buffer.getInt());
		message.setInterval(buffer.getInt());
		message.setUndone(buffer.getInt());
		message.setDone(buffer.getInt());
		message.setPeers(PeerUtils.read(buffer, size));
		TrackerManager.getInstance().announce(message);
	}

}
