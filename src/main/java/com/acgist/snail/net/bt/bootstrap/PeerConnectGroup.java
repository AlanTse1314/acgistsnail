package com.acgist.snail.net.bt.bootstrap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.bt.peer.bootstrap.PeerLauncherMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;

/**
 * <p>Peer连接组：上传</p>
 * <p>
 * 对连接请求下载的PeerConnect管理优化：<br>
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
	
	private final BlockingQueue<PeerConnect> peerConnects;
	
	private PeerConnectGroup() {
		this.peerConnects = new LinkedBlockingQueue<>();
	}
	
	public static final PeerConnectGroup newInstance(TorrentSession torrentSession) {
		return new PeerConnectGroup();
	}
	
	/**
	 * <p>是否创建成功</p>
	 * <p>如果Peer当前提供下载，可以直接给予上传，否者将验证是否超过了连接的最大数量。</p>
	 */
	public boolean newPeerConnect(PeerSession peerSession, PeerLauncherMessageHandler peerLauncherMessageHandler) {
		synchronized (this.peerConnects) {
			if(!peerSession.downloading()) {
				if(this.peerConnects.size() >= SystemConfig.getPeerSize()) {
					LOGGER.debug("Peer连接数超过最大连接数量，拒绝连接：{}-{}", peerSession.host(), peerSession.peerPort());
					return false;
				}
			}
			final PeerConnect session = PeerConnect.newInstance(peerSession, peerLauncherMessageHandler);
			peerSession.status(PeerConfig.STATUS_UPLOAD);
			this.peerConnects.add(session);
		}
		return true;
	}
	
	/**
	 * 优化
	 */
	public void optimize() {
		LOGGER.debug("优化PeerConnect");
		synchronized (this.peerConnects) {
			try {
				inferiorPeerConnect();
			} catch (Exception e) {
				LOGGER.error("优化PeerConnect异常", e);
			}
		}
	}
	
	/**
	 * 释放资源
	 */
	public void release() {
		LOGGER.debug("释放PeerConnectGroup");
		synchronized (this.peerConnects) {
			this.peerConnects.forEach(connect -> {
				SystemThreadContext.submit(() -> {
					connect.release();
				});
			});
			this.peerConnects.clear();
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
	private void inferiorPeerConnect() {
		final int size = this.peerConnects.size();
		int index = 0;
		PeerConnect tmp = null; // 临时
		while(true) {
			if(index++ >= size) {
				break;
			}
			tmp = this.peerConnects.poll();
			if(tmp == null) {
				break;
			}
			if(!tmp.getPeerLauncherMessageHandler().available()) { // 不可用直接剔除
				inferiorPeerConnect(tmp);
				continue;
			}
			if(size < SystemConfig.getPeerSize()) {
				continue;
			}
			if(tmp.getPeerSession().downloading()) { // TODO：是否清除
				this.peerConnects.offer(tmp);
				continue;
			}
			final long mark = tmp.mark();
			if(mark == 0L) {
				inferiorPeerConnect(tmp);
			} else {
				this.peerConnects.offer(tmp);
			}
		}
	}
	
	private void inferiorPeerConnect(PeerConnect peerConnect) {
		if(peerConnect != null) {
			final PeerSession peerSession = peerConnect.getPeerSession();
			LOGGER.debug("剔除无效PeerConnect：{}-{}", peerSession.host(), peerSession.peerPort());
			peerConnect.release();
		}
	}
	
}
