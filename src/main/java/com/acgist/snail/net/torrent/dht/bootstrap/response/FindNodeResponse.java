package com.acgist.snail.net.torrent.dht.bootstrap.response;

import java.util.List;

import com.acgist.snail.net.torrent.dht.bootstrap.Request;
import com.acgist.snail.net.torrent.dht.bootstrap.Response;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.config.DhtConfig;

/**
 * 返回最近的8个Node
 * 
 * @author acgist
 * @since 1.0.0
 */
public class FindNodeResponse extends Response {

	private FindNodeResponse(byte[] t) {
		super(t);
	}
	
	private FindNodeResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final FindNodeResponse newInstance(Response response) {
		return new FindNodeResponse(response);
	}

	public static final FindNodeResponse newInstance(Request request) {
		return new FindNodeResponse(request.getT());
	}
	
	/**
	 * 获取节点，同时加入系统节点。
	 */
	public List<NodeSession> getNodes() {
		final byte[] bytes = this.getBytes(DhtConfig.KEY_NODES);
		return deserializeNodes(bytes);
	}
	
}
