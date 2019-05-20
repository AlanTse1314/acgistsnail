package com.acgist.snail.downloader.torrent.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.peer.PeerClient;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.manager.PeerManager;

/**
 * <p>PeerClient组：下载</p>
 * <p>
 * 对正在进行下载的PeerClient管理：<br>
 * <ul>
 * 	<li>创建PeerClient。</li>
 * 	<li>定时替换下载最慢的PeerClient。</li>
 * </ul>
 * </p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerClientGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerClientGroup.class);
	
	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
	/**
	 * PeerClient下载队列
	 */
	private final BlockingQueue<PeerClient> peerClients;
	/**
	 * 优选的Peer，每次优化时挑选出来可以进行下载的Peer，在优化后发送Pex消息发送给连接的Peer，发送完成后清空。
	 */
	private final List<PeerSession> optimize = new ArrayList<>();
	
	private PeerClientGroup(TorrentSession torrentSession) {
		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
		this.peerClients = new LinkedBlockingQueue<>();
	}
	
	public static final PeerClientGroup newInstance(TorrentSession torrentSession) {
		return new PeerClientGroup(torrentSession);
	}

	/**
	 * <p>优化PeerClient</p>
	 * <p>
	 * 挑选权重最低的PeerClient，剔除下载队列，将剔除的Peer插入到Peer队列头部，然后重新生成一个PeerClient。
	 * </p>
	 * 
	 * @return 当前可以使用的PeerSession
	 */
	public void optimize() {
		LOGGER.debug("优化PeerClient");
		synchronized (this.peerClients) {
			try {
				inferiorPeerClient();
				buildPeerClients();
			} catch (Exception e) {
				LOGGER.error("优化PeerClient异常", e);
			}
		}
	}
	
	/**
	 * 获取优秀的PeerSession，同时清空旧数据。
	 */
	public List<PeerSession> optimizePeerSession() {
		final var list = new ArrayList<>(this.optimize);
		this.optimize.clear();
		return list;
	}

	/**
	 * <p>资源释放</p>
	 * <p>释放所有正在下载的PeerClient。</p>
	 */
	public void release() {
		LOGGER.debug("释放PeerClientGroup");
		synchronized (this.peerClients) {
			this.peerClients.forEach(client -> {
				SystemThreadContext.submit(() -> {
					client.release();
				});
			});
			this.peerClients.clear();
		}
	}
	
	/**
	 * 生成PeerClient列表，生成到不能继续生成为止。
	 */
	private void buildPeerClients() {
		LOGGER.debug("优化PeerClient-创建下载PeerClient");
		boolean ok = true;
		while(ok) {
			ok = buildPeerClient();
		}
	}
	
	/**
	 * <p>新建PeerClient加入下载队列，从Peer列表尾部拿出一个Peer创建下载。</p>
	 * <p>如果任务不处于下载状态、已经处于下载的PeerClient大于等于配置的最大PeerClient数量、不能查找到更多的Peer时返回不能继续生成。</p>
	 * 
	 * @return true-继续生成；false-不继续生成
	 */
	private boolean buildPeerClient() {
		if(!this.taskSession.download()) {
			return false;
		}
		if(this.peerClients.size() >= SystemConfig.getPeerSize()) {
			return false;
		}
		final PeerSession peerSession = PeerManager.getInstance().pick(this.torrentSession.infoHashHex());
		if(peerSession != null) {
			final PeerClient client = PeerClient.newInstance(peerSession, this.torrentSession);
			final boolean ok = client.download();
			if(ok) {
				peerSession.status(PeerConfig.STATUS_DOWNLOAD); // 设置下载中
				this.peerClients.add(client);
			} else { // 失败后需要放回队列。
				PeerManager.getInstance().inferior(this.torrentSession.infoHashHex(), peerSession);
			}
			return true;
		} else {
			return false;
		}
	}
	
	/***
	 * <p>选择劣质Peer，释放资源，然后将劣质Peer放入Peer队列头部。</p>
	 * <p>
	 * 挑选权重最低的PeerClient作为劣质Peer，如果其中含有不可用的PeerClient，直接剔除该PeerClient，
	 * 但是依旧需要循环完所有的PeerClient，清除权重进行新一轮的权重计算。
	 * 如果存在不可用的PeerClient时，则不剔除分数最低的PeerClient。
	 * </p>
	 * <p>
	 * 不可用的Peer：状态不可用或者下载量=0。
	 * </p>
	 */
	private void inferiorPeerClient() {
		LOGGER.debug("优化PeerClient-剔除劣质PeerClient");
		final int size = this.peerClients.size();
		if(size < SystemConfig.getPeerSize()) {
			return;
		}
		int index = 0;
		boolean unusable = false; // 不可用
		int mark = 0, minMark = 0;
		PeerClient tmp = null; // 临时
		PeerClient inferior = null; // 劣质PeerClient
		while(true) {
			if(index++ >= size) {
				break;
			}
			tmp = this.peerClients.poll();
			if(tmp == null) {
				break;
			}
			if(!tmp.available()) { // 如果当前挑选的是不可用的PeerClient不执行后面操作
				unusable = true;
				inferiorPeerClient(tmp);
				continue;
			}
			mark = tmp.mark(); // 清空权重
			if(mark > 0) { // 添加可用
				this.optimize.add(tmp.peerSession());
			} else { // 如果速度=0，直接剔除
				unusable = true;
				inferiorPeerClient(tmp);
				continue;
			}
			if(inferior == null) {
				inferior = tmp;
				minMark = mark;
			} else if(mark < minMark) {
				this.peerClients.offer(inferior);
				inferior = tmp;
				minMark = mark;
			} else {
				this.peerClients.offer(tmp);
			}
		}
		if(unusable) {
			if(inferior != null) {
				this.peerClients.offer(inferior);
			}
		} else {
			inferiorPeerClient(inferior);
		}
	}
	
	/**
	 * 剔除劣质Peer，释放资源，放入Peer队列头部。
	 */
	private void inferiorPeerClient(PeerClient peerClient) {
		if(peerClient != null) {
			final PeerSession peerSession = peerClient.peerSession();
			LOGGER.debug("剔除劣质PeerClient：{}-{}", peerSession.host(), peerSession.port());
			SystemThreadContext.submit(() -> {
				peerClient.release();
			});
			PeerManager.getInstance().inferior(this.torrentSession.infoHashHex(), peerSession);
		}
	}

}
