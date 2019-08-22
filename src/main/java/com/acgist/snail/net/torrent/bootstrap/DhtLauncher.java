package com.acgist.snail.net.torrent.bootstrap;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.dht.DhtClient;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>DHT任务：定时查询Peer</p>
 * <p>BT下载任务客户端连接时如果支持DHT，放入到{@link #dhtAddress}列表。</p>
 * <p>定时使用最近的可用节点和{@link #dhtAddress}查询Peer。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DhtLauncher implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(DhtLauncher.class);
	
	private final InfoHash infoHash;
	
	/**
	 * 客户端连接时支持DHT，加入列表，定时查询Peer时使用。
	 */
	private final List<InetSocketAddress> nodes = new ArrayList<>();
	
	private DhtLauncher(TorrentSession torrentSession) {
		this.infoHash = torrentSession.infoHash();
	}
	
	public static final DhtLauncher newInstance(TorrentSession torrentSession) {
		return new DhtLauncher(torrentSession);
	}
	
	@Override
	public void run() {
		LOGGER.debug("执行DHT定时任务");
		try {
			joinNodes();
			final var list = pick();
			findPeers(list);
		} catch (Exception e) {
			LOGGER.error("执行DHT定时任务异常", e);
		}
	}

	/**
	 * 将交换的节点加入到系统中。
	 */
	private void joinNodes() {
		this.nodes.forEach(address -> {
			NodeManager.getInstance().newNodeSession(address.getHostString(), address.getPort());
		});
	}
	
	/**
	 * 选择DHT客户端地址
	 */
	private List<InetSocketAddress> pick() {
		final List<InetSocketAddress> list = new ArrayList<>();
		synchronized (this.nodes) {
			if(CollectionUtils.isNotEmpty(this.nodes)) {
				list.addAll(this.nodes);
				this.nodes.clear();
			}
		}
		final var nodes = NodeManager.getInstance().findNode(this.infoHash.infoHash());
		if(CollectionUtils.isNotEmpty(nodes)) {
			for (NodeSession node : nodes) {
				list.add(NetUtils.buildSocketAddress(node.getHost(), node.getPort()));
			}
		}
		return list;
	}
	
	/**
	 * 查询Peer
	 */
	private void findPeers(List<InetSocketAddress> list) {
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		for (InetSocketAddress socketAddress : list) {
			final DhtClient client = DhtClient.newInstance(socketAddress);
			client.getPeers(this.infoHash.infoHash());
		}
	}
	
	/**
	 * Peer客户端添加DHT客户端
	 * 
	 * @param host 地址
	 * @param port 端口
	 */
	public void put(String host, Integer port) {
		synchronized (this.nodes) {
			this.nodes.add(NetUtils.buildSocketAddress(host, port));
		}
	}
	
}
