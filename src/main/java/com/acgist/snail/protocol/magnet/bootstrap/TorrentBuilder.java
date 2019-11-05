package com.acgist.snail.protocol.magnet.bootstrap;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * 种子文件创建
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentBuilder.class);
	
	/**
	 * InfoHash
	 */
	private final InfoHash infoHash;
	/**
	 * Tracker服务器
	 */
	private final List<String> trackers;
	
	private TorrentBuilder(InfoHash infoHash, List<String> trackers) {
		this.infoHash = infoHash;
		this.trackers = trackers;
	}
	
	public static final TorrentBuilder newInstance(InfoHash infoHash, List<String> trackers) {
		return new TorrentBuilder(infoHash, trackers);
	}
	
	/**
	 * <p>创建种子文件</p>
	 * 
	 * @param path 文件路径
	 */
	public String buildFile(String path) {
		final String filePath = FileUtils.file(path, fileName());
		final Map<String, Object> fileInfo = buildFileInfo();
		this.createFile(filePath, fileInfo);
		return filePath;
	}

	/**
	 * 种子信息
	 */
	private Map<String, Object> buildFileInfo() {
		final Map<String, Object> data = new LinkedHashMap<>();
		data.put(Torrent.COMMENT, SystemConfig.getSource());
		data.put(Torrent.COMMENT_UTF8, SystemConfig.getSource());
		data.put(Torrent.ENCODING, SystemConfig.DEFAULT_CHARSET);
		data.put(Torrent.CREATED_BY, SystemConfig.getNameEnAndVersion());
		data.put(Torrent.CREATION_DATE, DateUtils.unixTimestamp());
		this.announce(data);
		this.infoHash(data);
		this.node(data);
		return data;
	}

	/**
	 * 设置Tracker服务器列表
	 */
	private void announce(Map<String, Object> data) {
		if(CollectionUtils.isEmpty(this.trackers)) {
			return;
		}
		if(this.trackers.size() > 0) {
			data.put(Torrent.ANNOUNCE, this.trackers.get(0));
		}
		if(this.trackers.size() > 1) {
			data.put(
				Torrent.ANNOUNCE_LIST,
				this.trackers.subList(1, this.trackers.size()).stream()
					.map(value -> List.of(value))
					.collect(Collectors.toList())
			);
		}
	}
	
	/**
	 * 设置InfoHash
	 */
	private void infoHash(Map<String, Object> data) {
		try {
			final var decoder = BEncodeDecoder.newInstance(this.infoHash.info());
			data.put(Torrent.INFO, decoder.nextMap());
		} catch (NetException e) {
			LOGGER.error("设置InfoHash异常", e);
		}
	}

	/**
	 * 设置DHT节点
	 */
	private void node(Map<String, Object> data) {
		final var sessions = NodeManager.getInstance().findNode(this.infoHash.infoHash());
		if(CollectionUtils.isNotEmpty(sessions)) {
			final var nodes = sessions.stream()
				.filter(session -> NetUtils.isIp(session.getHost()))
				.map(session -> List.of(session.getHost(), session.getPort()))
				.collect(Collectors.toList());
			if(CollectionUtils.isNotEmpty(nodes)) {
				data.put(Torrent.NODES, nodes);
			}
		}
	}
	
	/**
	 * 文件名称
	 */
	private String fileName() {
		return this.infoHash.infoHashHex() + Protocol.Type.TORRENT.defaultSuffix();
	}

	/**
	 * 保存种子文件
	 * 
	 * @param filePath 文件路径
	 * @param fileInfo 数据
	 */
	private void createFile(String filePath, Map<String, Object> fileInfo) {
		final File file = new File(filePath);
		// 文件已存在时不创建
		if(file.exists()) {
			return;
		}
		LOGGER.debug("保存种子文件：{}", filePath);
		final byte[] bytes = BEncodeEncoder.encodeMap(fileInfo);
		FileUtils.write(filePath, bytes);
	}

}
