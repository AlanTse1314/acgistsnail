package com.acgist.snail.net.torrent.bootstrap;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>Tracker执行器</p>
 * <p>使用TrackerClient查询Peer信息</p>
 * 
 * @author acgist
 */
public final class TrackerLauncher {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerLauncher.class);
	
	/**
	 * <p>transaction_id</p>
	 * <p>ID与Tracker服务器和BT任务一一对应</p>
	 */
	private final Integer id;
	/**
	 * <p>下次等待时间</p>
	 */
	private Integer interval;
	/**
	 * <p>已完成数量</p>
	 */
	private Integer seeder;
	/**
	 * <p>未完成数量</p>
	 */
	private Integer leecher;
	/**
	 * <p>可用状态</p>
	 */
	private boolean available = true;
	/**
	 * <p>是否需要释放</p>
	 * <p>查找Peer后需要释放</p>
	 */
	private boolean needRelease = false;
	/**
	 * <p>Tracker客户端</p>
	 */
	private final TrackerClient client;
	/**
	 * <p>BT任务信息</p>
	 */
	private final TorrentSession torrentSession;
	
	/**
	 * @param client Tracker客户端
	 * @param torrentSession BT任务信息
	 */
	private TrackerLauncher(TrackerClient client, TorrentSession torrentSession) {
		this.id = NumberUtils.build();
		this.client = client;
		this.torrentSession = torrentSession;
	}
	
	/**
	 * <p>创建Tracker执行器</p>
	 * 
	 * @param client Tracker客户端
	 * @param torrentSession BT任务信息
	 * 
	 * @return Tracker执行器
	 */
	public static final TrackerLauncher newInstance(TrackerClient client, TorrentSession torrentSession) {
		return new TrackerLauncher(client, torrentSession);
	}

	/**
	 * <p>获取ID</p>
	 * 
	 * @return ID
	 */
	public Integer id() {
		return this.id;
	}
	
	/**
	 * <p>获取声明地址</p>
	 * 
	 * @return 声明地址
	 */
	public String announceUrl() {
		return this.client.announceUrl();
	}

	/**
	 * <p>查找Peer</p>
	 */
	public void findPeer() {
		this.needRelease = true;
		if(this.available()) {
			LOGGER.debug("TrackerLauncher查找Peer：{}", this.announceUrl());
			this.client.findPeers(this.id, this.torrentSession);
		}
	}

	/**
	 * <p>收到声明响应消息</p>
	 * 
	 * @param message 声明响应消息
	 */
	public void announce(AnnounceMessage message) {
		if(message == null) {
			return;
		}
		if(!this.available()) {
			LOGGER.debug("收到声明响应消息：Tracker执行器无效");
			return;
		}
		this.interval = message.getInterval();
		this.seeder = message.getSeeder();
		this.leecher = message.getLeecher();
		this.peer(message.getPeers());
		LOGGER.debug(
			"{}-收到声明响应：做种Peer数量：{}，下载Peer数量：{}，下次请求时间：{}",
			this.announceUrl(),
			this.seeder,
			this.leecher,
			this.interval
		);
	}
	
	/**
	 * <p>添加Peer</p>
	 * 
	 * @param peers Peer列表
	 */
	private void peer(Map<String, Integer> peers) {
		if(MapUtils.isEmpty(peers)) {
			return;
		}
		final PeerManager manager = PeerManager.getInstance();
		peers.forEach((host, port) -> manager.newPeerSession(
			this.torrentSession.infoHashHex(),
			this.torrentSession.statistics(),
			host,
			port,
			PeerConfig.SOURCE_TRACKER
		));
	}

	/**
	 * <p>释放资源</p>
	 * <p>暂停发送stop消息、完成发送complete消息</p>
	 */
	public void release() {
		if(this.needRelease && this.available()) {
			this.available = false;
			this.needRelease = false;
			try {
				if(this.torrentSession.completed()) { // 任务完成
					LOGGER.debug("Tracker完成通知：{}", this.announceUrl());
					this.client.complete(this.id, this.torrentSession);
				} else { // 任务暂停
					LOGGER.debug("Tracker暂停通知：{}", this.announceUrl());
					this.client.stop(this.id, this.torrentSession);
				}
			} catch (NetException e) {
				LOGGER.error("TrackerLauncher关闭异常", e);
			}
			TrackerManager.getInstance().removeTrackerLauncher(this.id);
		}
	}
	
	/**
	 * <p>获取是否可用</p>
	 * <p>可用状态：TrackerLauncher可用、TrackerClient可用</p>
	 * 
	 * @return true-可用；false-不可用；
	 */
	private boolean available() {
		return this.available && this.client.available();
	}
	
}
