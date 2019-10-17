package com.acgist.snail.net.torrent.tracker.bootstrap.impl;

import java.net.http.HttpResponse.BodyHandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerService;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.message.HttpAnnounceMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Tracker HTTP客户端</p>
 * <p>Tracker Returns Compact Peer Lists</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0023.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class HttpTrackerClient extends TrackerClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpTrackerClient.class);
	
	/**
	 * 刮檫URL
	 */
	private static final String SCRAPE_URL_SUFFIX = "/scrape";
	/**
	 * 声明URL
	 */
	private static final String ANNOUNCE_URL_SUFFIX = "/announce";
	
	/**
	 * 跟踪器ID，收到第一次响应时获取，以后每次发送声明消息时上送。
	 */
	private String trackerId;
	
	private HttpTrackerClient(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Protocol.Type.http);
	}

	public static final HttpTrackerClient newInstance(String announceUrl) throws NetException {
		final String scrapeUrl = buildScrapeUrl(announceUrl);
		return new HttpTrackerClient(scrapeUrl, announceUrl);
	}

	@Override
	public void announce(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.started);
		final var response = HTTPClient.get(announceMessage, BodyHandlers.ofString(), TrackerClient.TIMEOUT);
		if(!HTTPClient.ok(response)) {
			throw new NetException("HTTP获取Peer失败");
		}
		// TODO：解决B编码有多余数据问题，测试地址：http://tracker3.itzmx.com:6961/announce
		final String body = response.body();
		final var decoder = BEncodeDecoder.newInstance(body.getBytes());
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("HttpTracker消息格式错误：{}", decoder.oddString());
			return;
		}
		final var httpAnnounceMessage = HttpAnnounceMessage.valueOf(decoder);
		this.trackerId = httpAnnounceMessage.getTrackerId();
		final AnnounceMessage message = httpAnnounceMessage.toAnnounceMessage(sid);
		TrackerManager.getInstance().announce(message);
	}

	@Override
	public void complete(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.completed);
		HTTPClient.get(announceMessage, BodyHandlers.ofString(), TrackerClient.TIMEOUT);
	}
	
	@Override
	public void stop(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.stopped);
		HTTPClient.get(announceMessage, BodyHandlers.ofString(), TrackerClient.TIMEOUT);
	}
	
	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		// TODO：刮檫
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected String buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long download, long remain, long upload) {
		final StringBuilder builder = new StringBuilder(this.announceUrl);
		builder.append("?")
			.append("info_hash").append("=").append(torrentSession.infoHash().infoHashURL()).append("&") // InfoHash
			.append("peer_id").append("=").append(PeerService.getInstance().peerIdUrl()).append("&") // PeerID
			.append("port").append("=").append(SystemConfig.getTorrentPortExtShort()).append("&") // 外网Peer端口
			.append("uploaded").append("=").append(upload).append("&") // 已上传大小
			.append("downloaded").append("=").append(download).append("&") // 已下载大小
			.append("left").append("=").append(remain).append("&") // 剩余下载大小
			.append("compact").append("=").append("1").append("&") // 默认：1（紧凑）
			.append("event").append("=").append(event.name()).append("&") // 事件：started、completed、stopped
			.append("numwant").append("=").append(WANT_PEER_SIZE); // 想要获取的Peer数量
		if(StringUtils.isNotEmpty(this.trackerId)) {
			builder.append("&").append("trackerid").append("=").append(this.trackerId); // 跟踪器ID
		}
		return builder.toString();
	}
	
	/**
	 * <p>创建scrapeUrl</p>
	 * <p>announceUrl转换scrapeUrl：</p>
	 * <pre>
	 *	~http://example.com/announce			-> ~http://example.com/scrape
	 *	~http://example.com/x/announce			-> ~http://example.com/x/scrape
	 *	~http://example.com/announce.php		-> ~http://example.com/scrape.php
	 *	~http://example.com/a					-> (scrape not supported)
	 *	~http://example.com/announce?x2%0644	-> ~http://example.com/scrape?x2%0644
	 *	~http://example.com/announce?x=2/4		-> (scrape not supported)
	 *	~http://example.com/x%064announce		-> (scrape not supported)
	 * </pre>
	 */
	private static final String buildScrapeUrl(String url) {
		if(url.contains(ANNOUNCE_URL_SUFFIX)) {
			return url.replace(ANNOUNCE_URL_SUFFIX, SCRAPE_URL_SUFFIX);
		}
		return null;
	}

}
