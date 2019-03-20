package com.acgist.snail.pojo.message;

import java.util.Map;

/**
 * tracker announce返回信息
 */
public class AnnounceMessage {

	private Integer id; // id
	private Integer interval; // 下次等待时间
	private Integer done; // 已完成数量
	private Integer undone; // 未完成数量
	private Map<String, Integer> peers; // Peers数据

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public Integer getDone() {
		return done;
	}

	public void setDone(Integer done) {
		this.done = done;
	}

	public Integer getUndone() {
		return undone;
	}

	public void setUndone(Integer undone) {
		this.undone = undone;
	}

	public Map<String, Integer> getPeers() {
		return peers;
	}

	public void setPeers(Map<String, Integer> peers) {
		this.peers = peers;
	}

}
