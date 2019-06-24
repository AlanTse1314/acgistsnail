package com.acgist.snail.net.bt.peer.bootstrap;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.bt.peer.bootstrap.ltep.PeerExchangeMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>Peer管理器</p>
 * <p>Peer加入放入两个队列，一个队列负责下载时使用：{@link #peers}，一个负责存档：{@link #storagePeers}。</p>
 * TODO：将IP分段，然后每次下载有连接分析时进行评分，插入IP时按照分段插入。
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerManager.class);

	private static final PeerManager INSTANCE = new PeerManager();
	
	/**
	 * <p>使用的Peer，下载时Peer从中间剔除，选为劣质Peer时放回到列表中。</p>
	 * <p>key=infoHashHex</p>
	 * <p>value=Peers：双端队列，新加入插入队尾，剔除的Peer插入对头。</p>
	 */
	private final Map<String, Deque<PeerSession>> peers;
	/**
	 * Peer存档队列。
	 */
	private final Map<String, List<PeerSession>> storagePeers;
	
	private PeerManager() {
		this.peers = new ConcurrentHashMap<>();
		this.storagePeers = new ConcurrentHashMap<>();
	}
	
	public static final PeerManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 获取所有Peer信息。
	 */
	public Map<String, List<PeerSession>> peers() {
		synchronized (this.storagePeers) {
			return this.storagePeers.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> {
					return new ArrayList<>(entry.getValue());
				}));
		}
	}
	
	/**
	 * 删除任务对应的Peer列表
	 */
	public void remove(String infoHashHex) {
		synchronized (this.peers) {
			this.peers.remove(infoHashHex);
		}
		synchronized (this.storagePeers) {
			this.storagePeers.remove(infoHashHex);
		}
	}
	
	/**
	 * 新增Peer，插入尾部。
	 * 
	 * @param infoHashHex 下载文件infoHashHex
	 * @param parent torrent下载统计
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @return PeerSession，如果是本机IP返回null。
	 */
	public PeerSession newPeerSession(String infoHashHex, StatisticsSession parent, String host, Integer port, byte source) {
		var deque = deque(infoHashHex);
		synchronized (deque) {
			PeerSession peerSession = findPeerSession(infoHashHex, host);
			if(peerSession == null) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("添加PeerSession，{}-{}，来源：{}", host, port, PeerConfig.source(source));
				}
				peerSession = PeerSession.newInstance(parent, host, port);
				deque.offerLast(peerSession); // 下载Peer列表
				list(infoHashHex).add(peerSession); // 存档
			}
			peerSession.source(source); // 设置来源
			return peerSession;
		}
	}
	
	/**
	 * 放入一个劣质的Peer，插入头部。
	 */
	public void inferior(String infoHashHex, PeerSession peerSession) {
		var deque = deque(infoHashHex);
		synchronized (deque) {
			deque.offerFirst(peerSession);
		}
	}
	
	/**
	 * 放入一个优质的Peer，插入尾部。
	 */
	public void preference(String infoHashHex, PeerSession peerSession) {
		var deque = deque(infoHashHex);
		synchronized (deque) {
			deque.offerLast(peerSession);
		}
	}
	
	/**
	 * 从尾部选择一个Peer下载，选择可用状态的Peer。
	 */
	public PeerSession pick(String infoHashHex) {
		var deque = deque(infoHashHex);
		synchronized (deque) {
			int index = 0;
			final int size = deque.size();
			PeerSession peerSession = null;
			while(true) {
				if(++index > size) {
					break;
				}
				peerSession = deque.pollLast();
				if(peerSession.available()) { // 可用
					return peerSession;
				} else {
					deque.offerFirst(peerSession);
				}
			}
			return null;
		}
	}
	
	/**
	 * <p>发送have消息</p>
	 * <p>只发送给当前上传和下载的Peer。</p>
	 */
	public void have(String infoHashHex, int index) {
		final var list = list(infoHashHex);
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		LOGGER.debug("发送Have消息，通知Peer数量：{}", list.size());
		list.stream()
		.filter(session -> session.uploading() || session.downloading())
		.forEach(session -> {
			var handler = session.peerLauncherMessageHandler();
			if(handler != null && handler.available()) {
				handler.have(index);
			}
		});
	}
	
	/**
	 * <p>发送PEX消息</p>
	 * <p>只发送给当前上传和下载的Peer。</p>
	 */
	public void exchange(String infoHashHex, List<PeerSession> optimize) {
		final byte[] bytes = PeerExchangeMessageHandler.buildMessage(optimize);
		if(bytes == null) {
			return;
		}
		final var list = list(infoHashHex);
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		LOGGER.debug("发送PEX消息，Peer数量：{}，通知Peer数量：{}", optimize.size(), list.size());
		list.stream()
		.filter(session -> session.uploading() || session.downloading())
		.forEach(session -> {
			var handler = session.peerLauncherMessageHandler();
			if(handler != null && handler.available()) {
				handler.exchange(bytes);
			}
		});
	}
	
	/**
	 * 获取对应的Peer列表
	 */
	private Deque<PeerSession> deque(String infoHashHex) {
		synchronized (this.peers) {
			Deque<PeerSession> deque = this.peers.get(infoHashHex);
			if(deque == null) {
				deque = new LinkedBlockingDeque<>();
				this.peers.put(infoHashHex, deque);
			}
			return deque;
		}
	}
	
	/**
	 * 获取对应的Peer列表（存储）
	 */
	public List<PeerSession> list(String infoHashHex) {
		synchronized (this.storagePeers) {
			List<PeerSession> list = this.storagePeers.get(infoHashHex);
			if(list == null) {
				list = new ArrayList<>();
				this.storagePeers.put(infoHashHex, list);
			}
			return list;
		}
	}
	
	/**
	 * 是否已经添加过
	 */
	private PeerSession findPeerSession(String infoHashHex, String host) {
		final var list = list(infoHashHex);
		final Optional<PeerSession> optional = list.stream().filter(peer -> {
			return peer.equals(host);
		}).findFirst();
		if(optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

}
