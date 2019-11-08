package com.acgist.snail.pojo.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.config.SystemConfig;

/**
 * <p>文件列表信息</p>
 * <p>单文件时files为空</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentInfo {

	/**
	 * 填充文件前缀（不需要下载和显示）
	 */
	public static final String PADDING_FILE_PREFIX = "_____padding_file";
	/**
	 * 私有种子
	 */
	public static final byte PRIVATE_TORRENT = 1;
	
	/**
	 * 名称（单文件）
	 */
	private String name;
	/**
	 * 名称UTF8（单文件）
	 */
	private String nameUtf8;
	/**
	 * 文件大小（单文件）
	 */
	private Long length;
	/**
	 * ed2k（单文件）
	 */
	private byte[] ed2k;
	/**
	 * filehash（单文件）
	 */
	private byte[] filehash;
	/**
	 * <p>特征信息</p>
	 * <p>所有块的hash合并</p>
	 */
	private byte[] pieces;
	/**
	 * 每个块大小
	 */
	private Long pieceLength;
	/**
	 * 发布者
	 */
	private String publisher;
	/**
	 * 发布者UTF8
	 */
	private String publisherUtf8;
	/**
	 * 发布者URL
	 */
	private String publisherUrl;
	/**
	 * 发布者URL UTF8
	 */
	private String publisherUrlUtf8;
	/**
	 * 私有种子
	 */
	private Long privateTorrent;
	/**
	 * 文件列表（多文件）
	 */
	private List<TorrentFile> files;

	protected TorrentInfo() {
	}

	public static final TorrentInfo valueOf(Map<String, Object> map) {
		if(map == null) {
			return null;
		}
		final TorrentInfo info = new TorrentInfo();
		info.setName(BEncodeDecoder.getString(map, "name"));
		info.setNameUtf8(BEncodeDecoder.getString(map, "name.utf-8"));
		info.setLength(BEncodeDecoder.getLong(map, "length"));
		info.setEd2k(BEncodeDecoder.getBytes(map, "ed2k"));
		info.setFilehash(BEncodeDecoder.getBytes(map, "filehash"));
		info.setPieces(BEncodeDecoder.getBytes(map, "pieces"));
		info.setPieceLength(BEncodeDecoder.getLong(map, "piece length"));
		info.setPublisher(BEncodeDecoder.getString(map, "publisher"));
		info.setPublisherUtf8(BEncodeDecoder.getString(map, "publisher.utf-8"));
		info.setPublisherUrl(BEncodeDecoder.getString(map, "publisher-url"));
		info.setPublisherUrlUtf8(BEncodeDecoder.getString(map, "publisher-url.utf-8"));
		info.setPrivateTorrent(BEncodeDecoder.getLong(map, "private"));
		final List<Object> files = BEncodeDecoder.getList(map, "files");
		if(files != null) {
			info.setFiles(files(files));
		} else {
			info.setFiles(new ArrayList<>());
		}
		return info;
	}
	
	/**
	 * Piece总数量
	 */
	public int pieceSize() {
		return this.pieces.length / SystemConfig.SHA1_HASH_LENGTH;
	}
	
	/**
	 * 是否是私有种子
	 */
	public boolean isPrivateTorrent() {
		return this.privateTorrent == null ? false : this.privateTorrent.byteValue() == PRIVATE_TORRENT;
	}
	
	/**
	 * 列出下载文件（兼容单个文件）
	 */
	public List<TorrentFile> files() {
		if (this.files.isEmpty()) {
			final TorrentFile file = new TorrentFile();
			file.setEd2k(this.ed2k);
			file.setFilehash(this.filehash);
			file.setLength(this.length);
			if (this.name != null) {
				file.setPath(List.of(this.name));
			}
			if (this.nameUtf8 != null) {
				file.setPathUtf8(List.of(this.nameUtf8));
			}
			return List.of(file);
		} else {
			return this.files;
		}
	}
	
	/**
	 * <p>读取文件列表</p>
	 * <p>每个元素都是一个map</p>
	 */
	private static final List<TorrentFile> files(List<Object> files) {
		return files.stream()
			.map(value -> {
				return (Map<?, ?>) value;
			})
			.map(value -> {
				return TorrentFile.valueOf(value);
			})
			.collect(Collectors.toList());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameUtf8() {
		return nameUtf8;
	}

	public void setNameUtf8(String nameUtf8) {
		this.nameUtf8 = nameUtf8;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	public byte[] getEd2k() {
		return ed2k;
	}
	
	public void setEd2k(byte[] ed2k) {
		this.ed2k = ed2k;
	}

	public byte[] getFilehash() {
		return filehash;
	}

	public void setFilehash(byte[] filehash) {
		this.filehash = filehash;
	}

	public byte[] getPieces() {
		return pieces;
	}

	public void setPieces(byte[] pieces) {
		this.pieces = pieces;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPublisherUtf8() {
		return publisherUtf8;
	}

	public void setPublisherUtf8(String publisherUtf8) {
		this.publisherUtf8 = publisherUtf8;
	}

	public String getPublisherUrl() {
		return publisherUrl;
	}

	public void setPublisherUrl(String publisherUrl) {
		this.publisherUrl = publisherUrl;
	}

	public String getPublisherUrlUtf8() {
		return publisherUrlUtf8;
	}

	public void setPublisherUrlUtf8(String publisherUrlUtf8) {
		this.publisherUrlUtf8 = publisherUrlUtf8;
	}

	public Long getPrivateTorrent() {
		return privateTorrent;
	}

	public void setPrivateTorrent(Long privateTorrent) {
		this.privateTorrent = privateTorrent;
	}

	public Long getPieceLength() {
		return pieceLength;
	}

	public void setPieceLength(Long pieceLength) {
		this.pieceLength = pieceLength;
	}

	public List<TorrentFile> getFiles() {
		return files;
	}

	public void setFiles(List<TorrentFile> files) {
		this.files = files;
	}

}