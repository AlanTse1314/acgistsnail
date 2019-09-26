package com.acgist.snail.net.torrent.tracker.bootstrap.impl;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerService;
import com.acgist.snail.net.torrent.tracker.TrackerClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.ProtocolConfig.Protocol;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>Tracker UDP客户端</p>
 * <p>UDP Tracker Protocol for BitTorrent</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0015.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UdpTrackerClient extends com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient {

//	private static final Logger LOGGER = LoggerFactory.getLogger(UdpTrackerClient.class);
	
	private final String host;
	private final int port;
	/**
	 * 连接ID：获取Peer时使用
	 */
	private Long connectionId;
	private final TrackerClient trackerClient;

	private UdpTrackerClient(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Protocol.udp);
		URI uri = URI.create(announceUrl);
		this.host = uri.getHost();
		this.port = uri.getPort();
		this.trackerClient = TrackerClient.newInstance(NetUtils.buildSocketAddress(this.host, this.port));
	}

	public static final UdpTrackerClient newInstance(String announceUrl) throws NetException {
		return new UdpTrackerClient(announceUrl, announceUrl);
	}
	
	@Override
	public void announce(Integer sid, TorrentSession torrentSession) throws NetException {
		// 没有连接创建连接
		if(this.connectionId == null) {
			synchronized (this) {
				if(this.connectionId == null) {
					buildConnectionId();
				}
				ThreadUtils.wait(this, Duration.ofSeconds(com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient.TIMEOUT));
				if(this.connectionId == null) {
					throw new NetException("获取UdpTrackerClient connectionId失败");
				}
			}
		}
		send(buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.started));
	}

	@Override
	public void complete(Integer sid, TorrentSession torrentSession) throws NetException {
		if(this.connectionId != null) {
			send(buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.completed));
		}
	}
	
	@Override
	public void stop(Integer sid, TorrentSession torrentSession) throws NetException {
		if(this.connectionId != null) {
			send(buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.stopped));
		}
	}
	
	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		// TODO：刮檫
	}

	/**
	 * 设置connectionId
	 */
	public void connectionId(Long connectionId) {
		this.connectionId = connectionId;
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	/**
	 * 开始获取connectionId
	 */
	private void buildConnectionId() throws NetException {
		send(buildConnectMessage());
	}
	
	/**
	 * 发送数据
	 */
	private void send(ByteBuffer buffer) throws NetException {
		this.trackerClient.send(buffer);
	}

	/**
	 * 连接请求
	 */
	private ByteBuffer buildConnectMessage() {
		ByteBuffer buffer = ByteBuffer.allocate(16);
		// 必须等于：0x41727101980
		buffer.putLong(4497486125440L);
		buffer.putInt(TrackerConfig.Action.connect.action());
		buffer.putInt(this.id);
		return buffer;
	}
	
	/**
	 * Announce请求
	 */
	private ByteBuffer buildAnnounceMessage(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event) {
		long download = 0L, remain = 0L, upload = 0L;
		final var taskSession = torrentSession.taskSession();
		if(taskSession != null) {
			var statistics = taskSession.statistics();
			download = statistics.downloadSize();
			remain = taskSession.entity().getSize() - download;
			upload = statistics.uploadSize();
		}
		final ByteBuffer buffer = ByteBuffer.allocate(98);
		buffer.putLong(this.connectionId); // connection_id
		buffer.putInt(TrackerConfig.Action.announce.action()); // action
		buffer.putInt(sid); // transaction_id
		buffer.put(torrentSession.infoHash().infoHash()); // infoHash
		buffer.put(PeerService.getInstance().peerId()); // PeerId
		buffer.putLong(download); // 已下载大小
		buffer.putLong(remain); // 剩余下载大小
		buffer.putLong(upload); // 已上传大小
		buffer.putInt(event.event()); // 事件：started-2、completed-1、stopped-3
		buffer.putInt(0); // 本机IP：0（服务器自动获取）
		buffer.putInt(NumberUtils.build()); // 系统分配唯一键
		buffer.putInt(50); // 想要获取的Peer数量
		buffer.putShort(SystemConfig.getTorrentPortExtShort()); // 外网Peer端口
		return buffer;
	}

}
