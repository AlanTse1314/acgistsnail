package com.acgist.snail.net.dht.bootstrap.response;

import com.acgist.snail.net.dht.bootstrap.Response;
import com.acgist.snail.system.config.DhtConfig;

/**
 * 如果有Peer，返回Peer，否者返回最近的node
 * @author 28954
 *
 */
public class GetPeersResponse extends Response {

	public GetPeersResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final GetPeersResponse newInstance(byte[] bytes) {
		return new GetPeersResponse(Response.valueOf(bytes));
	}

	public String getToken() {
		return getString(DhtConfig.KEY_TOKEN);
	}
	
	public void getNodes() {
		// TODO：
	}
	
	public void getValues() {
		// TODO：
	}
	
}
