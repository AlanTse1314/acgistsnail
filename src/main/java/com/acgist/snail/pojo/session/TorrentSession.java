package com.acgist.snail.pojo.session;

import java.util.List;
import java.util.Map;

import com.acgist.snail.downloader.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.net.tracker.TrackerGroup;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentFile;
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
	private final Torrent torrent;
	/**
	 * 种子信息
	 */
	private final InfoHash infoHash;
	/**
	 * 任务
	 */
	private TaskSession taskSession;
	/**
	 * Tracker组
	 */
	private TrackerGroup trackerGroup;
	/**
	 * 文件下载组
	 */
	private TorrentStreamGroup torrentStreamGroup;

	private TorrentSession(Torrent torrent, InfoHash infoHash) throws DownloadException {
		if(torrent == null || infoHash == null) {
			throw new DownloadException("解析种子文件异常");
		}
		this.torrent = torrent;
		this.infoHash = infoHash;
	}

	public static final TorrentSession newInstance(Torrent torrent, InfoHash infoHash) throws DownloadException {
		return new TorrentSession(torrent, infoHash);
	}
	
	/**
	 * 开始下载任务：获取tracker、peer
	 */
	public void build(TaskSession taskSession) throws DownloadException {
		this.taskSession = taskSession;
		this.trackerGroup = new TrackerGroup(this);
		this.trackerGroup.loadTracker();
		this.torrentStreamGroup = TorrentStreamGroup.newInstance(taskSession.downloadFolder().getPath(), torrent, selectFiles());
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
	
	public TaskSession taskSession() {
		return this.taskSession;
	}
	
	public TrackerGroup trackerGroup() {
		return this.trackerGroup;
	}
	
	public TorrentStreamGroup torrentStreamGroup() {
		return this.torrentStreamGroup;
	}
	
	/**
	 * 获取选择的下载文件
	 */
	public List<TorrentFile> selectFiles() {
		final TorrentInfo info = torrent.getInfo();
		final List<TorrentFile> files = info.files();
		final List<String> selectedFiles = taskSession.downloadTorrentFiles();
		for (TorrentFile file : files) {
			if(selectedFiles.contains(file.path())) {
				file.select(true);
			} else {
				file.select(false);
			}
		}
		return files;
	}
	
	/**
	 * 释放资源
	 */
	public void release() {
		trackerGroup.release();
	}
	
	/**
	 * 设置Peer
	 */
	public void peer(Map<String, Integer> peers) {
		trackerGroup.peer(taskSession.statistics(), peers);
	}

}
