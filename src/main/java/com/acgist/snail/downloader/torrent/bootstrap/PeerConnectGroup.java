package com.acgist.snail.downloader.torrent.bootstrap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.peer.PeerMessageHandler;
import com.acgist.snail.pojo.session.PeerConnectSession;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;

/**
 * <p>Peer连接组：上传</p>
 * <p>
 * 对连接请求下载的PeerClient管理优化：<br>
 * <ul>
 * 	<li>清除长时间没有请求的Peer。</li>
 * 	<li>不能超过最大分享连接数（如果连接为当前下载的Peer可以忽略连接数）。</li>
 * </ul>
 * </p>
 * 
 * @author acgist
 * @since 1.0.2
 */
public class PeerConnectGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerConnectGroup.class);
	
	private final BlockingQueue<PeerConnectSession> peerConnectSessions;
	
	private PeerConnectGroup() {
		peerConnectSessions = new LinkedBlockingQueue<>();
	}
	
	public static final PeerConnectGroup newInstance(TorrentSession torrentSession) {
		return new PeerConnectGroup();
	}
	
	/**
	 * <p>是否创建成功</p>
	 * <p>如果Peer当前提供下载，可以直接给予上传，否者将验证是否超过了连接的最大数量。</p>
	 */
	public boolean newPeerConnect(PeerSession peerSession, PeerMessageHandler peerMessageHandler) {
		synchronized (this.peerConnectSessions) {
			if(!peerSession.downloading()) {
				if(this.peerConnectSessions.size() >= SystemConfig.getPeerSize()) {
					LOGGER.debug("Peer连接数超过最大连接数量，拒绝连接：{}-{}", peerSession.host(), peerSession.port());
					return false;
				}
			}
			final PeerConnectSession session = PeerConnectSession.newInstance(peerSession, peerMessageHandler);
			peerSession.status(PeerConfig.STATUS_UPLOAD);
			this.peerConnectSessions.add(session);
		}
		return true;
	}
	
	/**
	 * 优化
	 */
	public void optimize() {
		LOGGER.debug("优化PeerConnect");
		synchronized (this.peerConnectSessions) {
			inferiorPeerClient();	
		}
	}

	/**
	 * <p>剔除无效连接</p>
	 * <ul>
	 * 	<li>长时间没有请求。</li>
	 * </ul>
	 * <p>剔除时设置为阻塞。</p>
	 * 
	 * TODO：优化计算
	 */
	private void inferiorPeerClient() {
		final int size = this.peerConnectSessions.size();
		if(size < SystemConfig.getPeerSize()) {
			return;
		}
		int index = 0;
		PeerConnectSession tmp = null; // 临时
		while(true) {
			if(index++ >= size) {
				break;
			}
			tmp = this.peerConnectSessions.poll();
			if(tmp == null) {
				break;
			}
			if(tmp.getPeerSession().downloading()) { // TODO：是否清除
				this.peerConnectSessions.offer(tmp);
				continue;
			}
			final long mark = tmp.mark();
			if(mark == 0L) {
				inferiorPeerClient(tmp);
			} else {
				this.peerConnectSessions.offer(tmp);
			}
		}
	}
	
	private void inferiorPeerClient(PeerConnectSession peerConnectSession) {
		if(peerConnectSession != null) {
			final PeerSession peerSession = peerConnectSession.getPeerSession();
			final PeerMessageHandler handler = peerConnectSession.getPeerMessageHandler();
			LOGGER.debug("剔除无效PeerConnect：{}-{}", peerSession.host(), peerSession.port());
			handler.choke();
			handler.close();
			peerSession.unstatus(PeerConfig.STATUS_UPLOAD);
		}
	}
	
}
