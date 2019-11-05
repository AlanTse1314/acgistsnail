package com.acgist.snail.net.torrent.dht.bootstrap;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.ArrayUtils;

/**
 * <p>DHT管理器</p>
 * <p>管理内容：DHT请求</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DhtManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtManager.class);
	
	private static final DhtManager INSTANCE = new DhtManager();
	
	static {
		LOGGER.debug("启动DHT超时请求清除定时任务");
		SystemThreadContext.timerFixedDelay(DhtConfig.DHT_REQUEST_CLEAN_INTERVAL, DhtConfig.DHT_REQUEST_CLEAN_INTERVAL, TimeUnit.MINUTES, () -> {
			DhtManager.getInstance().clear(); // 清除DHT超时请求
		});
	}
	
	/**
	 * <p>请求列表</p>
	 */
	private final List<Request> requests;
	
	private DhtManager() {
		this.requests = new LinkedList<>();
	}
	
	public static final DhtManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>放入请求</p>
	 * <p>删除旧请求</p>
	 */
	public void put(Request request) {
		if(request == null) {
			return;
		}
		synchronized (this.requests) {
			final Request old = remove(request.getId());
			if(old != null) {
				LOGGER.warn("旧DHT请求没有收到响应（删除）");
			}
			this.requests.add(request);
		}
	}
	
	/**
	 * <p>设置响应</p>
	 * <p>删除并返回请求，同时设置Node为可用状态。</p>
	 */
	public Request response(Response response) {
		if(response == null) {
			return null;
		}
		NodeManager.getInstance().available(response);
		Request request = null;
		synchronized (this.requests) {
			request = remove(response.getId());
		}
		if(request == null) {
			return null;
		}
		request.setResponse(response);
		return request;
	}
	
	/**
	 * <p>清空DHT超时请求</p>
	 */
	public void clear() {
		LOGGER.debug("清空DHT超时请求");
		synchronized (this.requests) {
			Request request;
			final long timeout = DhtConfig.TIMEOUT.toMillis();
			final long timestamp = System.currentTimeMillis();
			final var iterator = this.requests.iterator();
			while(iterator.hasNext()) {
				request = iterator.next();
				if(timestamp - request.getTimestamp() > timeout) {
					iterator.remove();
				}
			}
		}
	}
	
	/**
	 * <p>移除请求</p>
	 */
	private Request remove(byte[] id) {
		Request request;
		final var iterator = this.requests.iterator();
		while(iterator.hasNext()) {
			request = iterator.next();
			if(ArrayUtils.equals(id, request.getId())) {
				iterator.remove();
				return request;
			}
		}
		return null;
	}

}
