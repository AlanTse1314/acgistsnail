package com.acgist.snail.pojo.message;

import java.util.Map;

import com.acgist.snail.utils.ObjectUtils;

/**
 * <p>Tracker声明响应消息</p>
 * <p>http://www.bittorrent.org/beps/bep_0015.html</p>
 * <p>https://wiki.theory.org/index.php/BitTorrentSpecification</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class AnnounceMessage {

	/**
	 * <p>id：transaction_id：TrackerId</p>
	 */
	private Integer id;
	/**
	 * TrackerId：HTTP Tracker
	 */
	private String trackerId;
	/**
	 * 下次请求等待时间
	 */
	private Integer interval;
	/**
	 * 做种Peer数量
	 */
	private Integer seeder;
	/**
	 * 下载Peer数量
	 */
	private Integer leecher;
	/**
	 * Peers数据（IP和端口）
	 */
	private Map<String, Integer> peers;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTrackerId() {
		return trackerId;
	}

	public void setTrackerId(String trackerId) {
		this.trackerId = trackerId;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public Integer getSeeder() {
		return seeder;
	}

	public void setSeeder(Integer seeder) {
		this.seeder = seeder;
	}

	public Integer getLeecher() {
		return leecher;
	}

	public void setLeecher(Integer leecher) {
		this.leecher = leecher;
	}

	public Map<String, Integer> getPeers() {
		return peers;
	}

	public void setPeers(Map<String, Integer> peers) {
		this.peers = peers;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this);
	}
	
}
