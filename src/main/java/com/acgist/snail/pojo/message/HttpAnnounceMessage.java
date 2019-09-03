package com.acgist.snail.pojo.message;

import java.util.Map;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.utils.PeerUtils;

/**
 * HTTP Tracker Announce消息
 * 
 * @author acgist
 * @since 1.0.0
 */
public class HttpAnnounceMessage {

	private String trackerId; // trackerId，返回后以后每次请求需要上送这个字段
	private String failureReason; // 失败原因
	private String warngingMessage; // 警告信息
	private Integer interval; // 下一次请求等待时间
	private Integer minInterval; // 下一次请求等待最小时间
	private Integer complete; // 已完成Peer数量
	private Integer incomplete; // 未完成Peer数量
	private Map<String, Integer> peers; // Peer数据（IP和端口）

	public static final HttpAnnounceMessage valueOf(Map<String, Object> map) {
		final HttpAnnounceMessage message = new HttpAnnounceMessage();
		message.setTrackerId(BEncodeDecoder.getString(map, "tracker id"));
		message.setFailureReason(BEncodeDecoder.getString(map, "failure reason"));
		message.setWarngingMessage(BEncodeDecoder.getString(map, "warnging message"));
		message.setInterval(BEncodeDecoder.getInteger(map, "interval"));
		message.setMinInterval(BEncodeDecoder.getInteger(map, "min interval"));
		message.setComplete(BEncodeDecoder.getInteger(map, "complete"));
		message.setIncomplete(BEncodeDecoder.getInteger(map, "incomplete"));
		message.setPeers(PeerUtils.read(BEncodeDecoder.getBytes(map, "peers")));
		return message;
	}
	
	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public String getWarngingMessage() {
		return warngingMessage;
	}

	public void setWarngingMessage(String warngingMessage) {
		this.warngingMessage = warngingMessage;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public Integer getMinInterval() {
		return minInterval;
	}

	public void setMinInterval(Integer minInterval) {
		this.minInterval = minInterval;
	}

	public String getTrackerId() {
		return trackerId;
	}

	public void setTrackerId(String trackerId) {
		this.trackerId = trackerId;
	}

	public Integer getComplete() {
		return complete;
	}

	public void setComplete(Integer complete) {
		this.complete = complete;
	}

	public Integer getIncomplete() {
		return incomplete;
	}

	public void setIncomplete(Integer incomplete) {
		this.incomplete = incomplete;
	}

	public Map<String, Integer> getPeers() {
		return peers;
	}

	public void setPeers(Map<String, Integer> peers) {
		this.peers = peers;
	}
	
	/**
	 * 转换为AnnounceMessage消息
	 * 
	 * @param sid Torrent和Tracker服务器对应的id
	 */
	public AnnounceMessage toAnnounceMessage(Integer sid) {
		final AnnounceMessage message = new AnnounceMessage();
		message.setId(sid);
		message.setInterval(this.getInterval());
		message.setDone(this.getComplete());
		message.setUndone(this.getIncomplete());
		message.setPeers(this.getPeers());
		return message;
	}

}
