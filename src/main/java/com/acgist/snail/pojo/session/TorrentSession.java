package com.acgist.snail.pojo.session;

import java.util.Map;

import com.acgist.snail.net.tracker.TrackerGroup;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.StringUtils;

/**
 * 种子session
 */
public class TorrentSession {
	
	/**
	 * 种子
	 */
	private Torrent torrent;
	/**
	 * 种子信息
	 */
	private InfoHash infoHash;
	/**
	 * 任务
	 */
	private TaskSession taskSession;
	/**
	 * Tracker组
	 */
	private TrackerGroup trackerGroup;

	private TorrentSession(Torrent torrent, InfoHash infoHash) throws DownloadException {
		if(torrent == null || infoHash == null) {
			throw new DownloadException("解析种子文件异常");
		}
		this.torrent = torrent;
		this.infoHash = infoHash;
		this.trackerGroup = new TrackerGroup();
	}

	public static final TorrentSession newInstance(Torrent torrent, InfoHash infoHash) throws DownloadException {
		return new TorrentSession(torrent, infoHash);
	}
	
	/**
	 * 开始加载tracker
	 */
	public void loadTracker() {
	}

	/**
	 * 下载名称
	 */
	public String name() {
		TorrentInfo torrentInfo = torrent.getInfo();
		String name = torrentInfo.getNameUtf8();
		if(StringUtils.isEmpty(name)) {
			name = StringUtils.charset(torrentInfo.getName(), torrent.getEncoding());
		}
		return name;
	}

	public Torrent torrent() {
		return this.torrent;
	}
	
	public InfoHash infoHash() {
		return this.infoHash;
	}
	
	public TrackerGroup trackerGroup() {
		return this.trackerGroup;
	}

	/**
	 * 设置Peer
	 */
	public void peer(Map<String, Integer> peers) {
		trackerGroup.peer(taskSession.statistics(), peers);
	}

}
