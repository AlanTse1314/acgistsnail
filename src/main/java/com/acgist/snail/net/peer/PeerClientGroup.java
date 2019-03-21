package com.acgist.snail.net.peer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;

/**
 * PeerClient分组<br>
 * 每次剔除下载速度最低的一个PeerClient<br>
 */
public class PeerClientGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerClientGroup.class);
	
	private List<PeerSession> peers = Collections.synchronizedList(new ArrayList<>());
	private List<PeerLauncher> launchers;

//	private PeerSession next() {
//	}

	/**
	 * 新增Peer
	 * @param parent torrent下载统计
	 * @param host 地址
	 * @param port 端口
	 */
	public void put(StatisticsSession parent, String host, Integer port) {
		synchronized (peers) {
			final boolean exist = peers.stream().anyMatch(peer -> {
				return peer.exist(host, port);
			});
			if(exist) {
				return;
			}
			LOGGER.debug("添加Peer，HOST：{}，PORT：{}", host, port);
			peers.add(new PeerSession(parent, host, port));
		}
	}

}
