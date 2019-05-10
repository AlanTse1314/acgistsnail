package com.acgist.snail.net.dht.bootstrap.response;

import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.Response;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.manager.NodeManager;

/**
 * Ping
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PingResponse extends Response {
	
	private PingResponse(byte[] t) {
		super(t);
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}

	private PingResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final PingResponse newInstance(Response response) {
		return new PingResponse(response);
	}

	public static final PingResponse newInstance(Request request) {
		return new PingResponse(request.getT());
	}
	
}
