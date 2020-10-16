package com.acgist.snail.context;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>数据流上下文</p>
 * <p>FTP、HTTP下载时读取数据时会阻塞线程，如果长时间没有数据交流会导致任务不能正常结束，定时查询并关闭没有使用的数据流来结束任务。</p>
 * 
 * @author acgist
 */
public final class StreamContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(StreamContext.class);
	
	private static final StreamContext INSTANCE = new StreamContext();
	
	public static final StreamContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>下载有效时间：{@value}</p>
	 */
	private static final long LIVE_TIME = 10 * DateUtils.ONE_SECOND;
	/**
	 * <p>下载有效时间（快速）：{@value}</p>
	 * <p>如果用户开始下载然后快速暂停，会导致出现任务等待中，只能等待定时任务来关闭任务。</p>
	 */
	private static final long LIVE_TIME_FAST = 2 * DateUtils.ONE_SECOND;
	/**
	 * <p>定时任务时间：{@value}</p>
	 */
	private static final long LIVE_CHECK_INTERVAL = 30 * DateUtils.ONE_SECOND;
	
	/**
	 * <p>数据流信息列表</p>
	 */
	private final List<StreamSession> sessions;
	
	private StreamContext() {
		this.sessions = new ArrayList<>();
		this.register();
	}
	
	/**
	 * <p>新建数据流信息</p>
	 * 
	 * @param input 数据流
	 * 
	 * @return 数据流信息
	 */
	public StreamSession newStreamSession(InputStream input) {
		final StreamSession session = new StreamSession(input);
		synchronized (this.sessions) {
			this.sessions.add(session);
		}
		return session;
	}
	
	/**
	 * <p>移除数据流信息</p>
	 * 
	 * @param session 数据流信息
	 */
	public void removeStreamSession(StreamSession session) {
		if(session != null) {
			synchronized (this.sessions) {
				this.sessions.remove(session);
			}
		}
	}

	/**
	 * <p>注册定时任务</p>
	 */
	private void register() {
		LOGGER.info("注册定时任务：数据流上下文管理");
		SystemThreadContext.timerAtFixedRate(
			LIVE_CHECK_INTERVAL,
			LIVE_CHECK_INTERVAL,
			TimeUnit.MILLISECONDS,
			new StreamCleanTimer(this.sessions)
		);
	}
	
	/**
	 * <p>数据流信息</p>
	 * 
	 * @author acgist
	 */
	public static final class StreamSession {
		
		/**
		 * <p>输入流</p>
		 */
		private final InputStream input;
		/**
		 * <p>最后一次心跳时间</p>
		 */
		private volatile long heartbeatTime;
	
		/**
		 * @param input 输入流
		 */
		private StreamSession(InputStream input) {
			this.input = input;
			this.heartbeatTime = System.currentTimeMillis();
		}
		
		/**
		 * <p>心跳设置</p>
		 */
		public void heartbeat() {
			this.heartbeatTime = System.currentTimeMillis();
		}

		/**
		 * <p>检查存活</p>
		 * 
		 * @return true-存活；false-死亡；
		 */
		public boolean checkLive() {
			return System.currentTimeMillis() - this.heartbeatTime <= LIVE_TIME;
		}

		/**
		 * <p>快速检测存活</p>
		 * <p>如果已经没有数据交互直接关闭数据流</p>
		 */
		public void fastCheckLive() {
			if(System.currentTimeMillis() - this.heartbeatTime > LIVE_TIME_FAST) {
				this.close();
			}
		}
		
		/**
		 * <p>关闭输入流</p>
		 */
		public void close() {
			try {
				LOGGER.info("输入流没有数据传输：关闭输入流");
				IoUtils.close(this.input);
			} catch (Exception e) {
				LOGGER.error("关闭输入流异常", e);
			} finally {
				StreamContext.getInstance().removeStreamSession(this);
			}
		}

	}
	
	/**
	 * <p>数据流清理定时任务</p>
	 * 
	 * @author acgist
	 */
	public static final class StreamCleanTimer implements Runnable {

		/**
		 * @see StreamContext#sessions
		 */
		private final List<StreamSession> sessions;
		
		/**
		 * @param sessions 数据流信息列表
		 */
		private StreamCleanTimer(List<StreamSession> sessions) {
			this.sessions = sessions;
		}
		
		@Override
		public void run() {
			LOGGER.debug("执行数据流清理定时任务");
			List<StreamSession> dieSessions;
			// 查找没有数据交流的任务
			synchronized (this.sessions) {
				dieSessions = this.sessions.stream()
					.filter(session -> !session.checkLive())
					.collect(Collectors.toList());
			}
			// 关闭无效任务
			dieSessions.forEach(StreamSession::close);
		}
		
	}
	
}
