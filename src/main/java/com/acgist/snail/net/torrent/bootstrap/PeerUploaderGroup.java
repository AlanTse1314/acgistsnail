package com.acgist.snail.net.torrent.bootstrap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;

/**
 * <p>PeerUploader组</p>
 * <dl>
 * 	<dt>管理PeerUploader</dt>
 * 	<dd>清除劣质Peer</dd>
 * 	<dd>管理连接数量</dd>
 * </dl>
 * 
 * @author acgist
 * @since 1.0.2
 */
public final class PeerUploaderGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerUploaderGroup.class);
	
	/**
	 * <p>PeerUploader队列</p>
	 */
	private final BlockingQueue<PeerUploader> peerUploaders = new LinkedBlockingQueue<>();
	/**
	 * <p>BT任务信息</p>
	 */
	private final TorrentSession torrentSession;
	
	private PeerUploaderGroup(TorrentSession torrentSession) {
		this.torrentSession = torrentSession;
	}
	
	/**
	 * <p>创建PeerUploader组</p>
	 * 
	 * @param torrentSession BT任务信息
	 * 
	 * @return PeerUploader组
	 */
	public static final PeerUploaderGroup newInstance(TorrentSession torrentSession) {
		return new PeerUploaderGroup(torrentSession);
	}
	
	/**
	 * <p>开始下载</p>
	 * <p>如果Peer接入支持下载则发送下载请求</p>
	 */
	public void download() {
		synchronized (this.peerUploaders) {
			this.peerUploaders.forEach(uploader -> uploader.download());
		}
	}
	
	/**
	 * <p>创建Peer接入连接</p>
	 * 
	 * @param peerSession Peer信息
	 * @param peerSubMessageHandler Peer消息代理
	 * 
	 * @return Peer接入
	 */
	public PeerUploader newPeerUploader(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		synchronized (this.peerUploaders) {
			LOGGER.debug("Peer接入：{}-{}", peerSession.host(), peerSession.port());
			if(!connectable(peerSession)) {
				LOGGER.debug("Peer接入失败：{}-{}", peerSession.host(), peerSession.port());
				return null;
			}
			final PeerUploader peerUploader = PeerUploader.newInstance(peerSession, this.torrentSession, peerSubMessageHandler);
			peerSession.status(PeerConfig.STATUS_UPLOAD);
			this.offer(peerUploader);
			return peerUploader;
		}
	}
	
	/**
	 * <dl>
	 * 	<dt>判断是否允许连接</dt>
	 * 	<dd>Peer当前正在下载</dd>
	 * 	<dd>当前连接小于最大连接数量</dd>
	 * </dl>
	 * 
	 * @param peerSession Peer信息
	 * 
	 * @return {@code true}-允许；{@code false}-不允许；
	 */
	private boolean connectable(PeerSession peerSession) {
		if(peerSession != null && peerSession.downloading()) {
			return true;
		} else {
			return this.peerUploaders.size() < SystemConfig.getPeerSize();
		}
	}
	
	/**
	 * <p>优化PeerUploader</p>
	 */
	public void optimize() {
		LOGGER.debug("优化PeerUploader");
		synchronized (this.peerUploaders) {
			try {
				inferiorPeerUploaders();
			} catch (Exception e) {
				LOGGER.error("优化PeerUploader异常", e);
			}
		}
	}
	
	/**
	 * <p>释放资源</p>
	 * <p>释放所有PeerUploader</p>
	 */
	public void release() {
		LOGGER.debug("释放PeerUploaderGroup");
		synchronized (this.peerUploaders) {
			this.peerUploaders.forEach(connect -> {
				SystemThreadContext.submit(() -> connect.release());
			});
			this.peerUploaders.clear();
		}
	}

	/**
	 * <p>剔除无效接入</p>
	 * <ul>
	 * 	<li>不可用的连接</li>
	 * 	<li>长时间没有请求的连接</li>
	 * 	<li>超过最大连接数的连接</li>
	 * </ul>
	 */
	private void inferiorPeerUploaders() {
		LOGGER.debug("剔除无效PeerUploader");
		int index = 0;
		int offerSize = 0; // 有效数量
		long uploadMark;
		long downloadMark;
		PeerUploader tmpDownloader;
		final int size = this.peerUploaders.size();
		final int maxSize = SystemConfig.getPeerSize();
		while(true) {
			if(index++ >= size) {
				break;
			}
			tmpDownloader = this.peerUploaders.poll();
			if(tmpDownloader == null) {
				break;
			}
			// 状态不可用直接剔除
			if(!tmpDownloader.available()) {
				inferiorPeerUploader(tmpDownloader);
				continue;
			}
			// 获取评分
			uploadMark = tmpDownloader.uploadMark(); // 上传评分
			downloadMark = tmpDownloader.downloadMark(); // 下载评分
			// 首次评分忽略
			if(!tmpDownloader.marked()) {
				offerSize++;
				this.offer(tmpDownloader);
				continue;
			}
			// 提供下载的Peer提供上传
			if(downloadMark > 0L) {
				offerSize++;
				this.offer(tmpDownloader);
				continue;
			}
			// 提供下载的Peer提供上传
			if(tmpDownloader.peerSession().downloading()) {
				offerSize++;
				this.offer(tmpDownloader);
				continue;
			}
			if(uploadMark <= 0L) {
				inferiorPeerUploader(tmpDownloader);
			} else if(offerSize > maxSize) {
				inferiorPeerUploader(tmpDownloader);
			} else {
				offerSize++;
				this.offer(tmpDownloader);
			}
		}
	}
	
	/**
	 * <p>PeerUploader加入队列</p>
	 * 
	 * @param peerUploader PeerUploader
	 */
	private void offer(PeerUploader peerUploader) {
		final var ok = this.peerUploaders.offer(peerUploader);
		if(!ok) {
			LOGGER.warn("PeerUploader丢失：{}", peerUploader);
		}
	}
	
	/**
	 * <p>剔除劣质Peer</p>
	 * 
	 * @param peerUploader 劣质Peer
	 */
	private void inferiorPeerUploader(PeerUploader peerUploader) {
		if(peerUploader != null) {
			final PeerSession peerSession = peerUploader.peerSession();
			LOGGER.debug("剔除无效PeerUploader：{}-{}", peerSession.host(), peerSession.port());
			SystemThreadContext.submit(() -> peerUploader.release());
		}
	}
	
}
