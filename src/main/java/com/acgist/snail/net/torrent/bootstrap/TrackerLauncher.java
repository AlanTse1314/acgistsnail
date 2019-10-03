package com.acgist.snail.net.torrent.bootstrap;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>Tracker执行器</p>
 * <p>使用TrackerClient查询Peer信息。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TrackerLauncher {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerLauncher.class);
	
	/**
	 * 客户端
	 */
	private final TrackerClient client;
	
	private final TorrentSession torrentSession;
//	private final TrackerLauncherGroup trackerLauncherGroup;
	
	/**
	 * transaction_id：连接时使用
	 */
	private final Integer id;
	/**
	 * 下次等待时间
	 */
	private Integer interval;
	/**
	 * 已完成数量
	 */
	private Integer done;
	/**
	 * 未完成数量
	 */
	private Integer undone;
	/**
	 * 可用状态
	 */
	private boolean available = true;
	/**
	 * 是否需要释放
	 */
	private boolean needRelease = false;
	
	private TrackerLauncher(TrackerClient client, TorrentSession torrentSession) {
		this.id = NumberUtils.build();
		this.client = client;
		this.torrentSession = torrentSession;
//		this.trackerLauncherGroup = torrentSession.trackerLauncherGroup();
	}
	
	public static final TrackerLauncher newInstance(TrackerClient client, TorrentSession torrentSession) {
		return new TrackerLauncher(client, torrentSession);
	}

	/**
	 * 获取ID
	 */
	public Integer id() {
		return this.id;
	}
	
	/**
	 * 获取声明地址
	 */
	public String announceUrl() {
		return this.client.announceUrl();
	}

	/**
	 * 查找Peer
	 */
	public void findPeer() {
		this.needRelease = true;
		if(available()) {
			LOGGER.debug("TrackerLauncher查找Peer：{}", this.client.announceUrl());
			this.client.findPeers(this.id, this.torrentSession);
		}
	}

	/**
	 * <p>收到声明响应</p>
	 */
	public void announce(AnnounceMessage message) {
		if(message == null) {
			return;
		}
		if(!available()) {
			return;
		}
		this.interval = message.getInterval();
		this.done = message.getDone();
		this.undone = message.getUndone();
		this.peer(message.getPeers());
		LOGGER.debug("{}-收到声明响应，已完成Peer数量：{}，未完成的Peer数量：{}，下次请求时间：{}", this.client.announceUrl(), this.done, this.undone, this.interval);
	}
	
	/**
	 * 添加Peer
	 */
	private void peer(Map<String, Integer> peers) {
		if(CollectionUtils.isEmpty(peers)) {
			return;
		}
		final PeerManager manager = PeerManager.getInstance();
		peers.forEach((host, port) -> {
			manager.newPeerSession(
				this.torrentSession.infoHashHex(),
				this.torrentSession.statistics(),
				host,
				port,
				PeerConfig.SOURCE_TRACKER);
		});
	}

	/**
	 * <p>释放资源</p>
	 * <p>发送stop消息，如果下载完成发送complete消息。</p>
	 */
	public void release() {
		if(this.needRelease && available()) {
			this.available = false;
			try {
				if(this.torrentSession.completed()) { // 任务完成
					LOGGER.debug("Tracker完成通知：{}", this.client.announceUrl());
					this.client.complete(this.id, this.torrentSession);
				} else { // 任务暂停
					LOGGER.debug("Tracker暂停通知：{}", this.client.announceUrl());
					this.client.stop(this.id, this.torrentSession);
				}
			} catch (Exception e) {
				LOGGER.error("TrackerLauncher释放异常", e);
			}
			TrackerManager.getInstance().release(this.id);
		}
	}
	
	/**
	 * <p>可用状态：释放资源并且TrackerClient可用。</p>
	 */
	private boolean available() {
		return this.available && this.client.available();
	}
	
}
