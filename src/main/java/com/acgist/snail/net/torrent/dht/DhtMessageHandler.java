package com.acgist.snail.net.torrent.dht;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.dht.bootstrap.DhtManager;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.net.torrent.dht.bootstrap.Request;
import com.acgist.snail.net.torrent.dht.bootstrap.Response;
import com.acgist.snail.net.torrent.dht.bootstrap.request.AnnouncePeerRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.request.FindNodeRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.request.GetPeersRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.request.PingRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.response.FindNodeResponse;
import com.acgist.snail.net.torrent.dht.bootstrap.response.GetPeersResponse;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.DhtConfig.ErrorCode;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>DHT消息代理</p>
 * <p>DHT Protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0005.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DhtMessageHandler extends UdpMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtMessageHandler.class);

	/**
	 * <p>获取响应</p>
	 */
	private static final Function<Request, Response> RESPONSE_GETTER = (request) -> {
		if(request == null) {
			return null;
		}
		final Response response = request.getResponse();
		if(response == null) {
			LOGGER.warn("DHT响应超时");
		} else if(!response.success()) {
			LOGGER.warn("DHT响应失败：{}-{}", response.errorCode(), response.errorMessage());
		}
		return response;
	};
	
	/**
	 * <p>判断响应是否成功</p>
	 */
	public static final Function<Response, Boolean> SUCCESS_VERIFY = (response) -> {
		return response != null && response.success();
	};
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		buffer.flip();
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final var decoder = BEncodeDecoder.newInstance(bytes);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("处理DHT消息错误（格式）：{}", decoder.oddString());
			return;
		}
		final String y = decoder.getString(DhtConfig.KEY_Y); // 消息类型
		if(DhtConfig.KEY_Q.equals(y)) {
			final Request request = Request.valueOf(decoder);
			request.setSocketAddress(socketAddress);
			onRequest(request, socketAddress);
		} else if(DhtConfig.KEY_R.equals(y)) {
			final Response response = Response.valueOf(decoder);
			response.setSocketAddress(socketAddress);
			onResponse(response);
		} else {
			LOGGER.warn("处理DHT消息错误（类型不支持）：{}", y);
		}
	}
	
	/**
	 * <p>处理请求</p>
	 * 
	 * @param request 请求
	 * @param socketAddress 客户端地址
	 */
	private void onRequest(final Request request, final InetSocketAddress socketAddress) {
		Response response = null;
		LOGGER.debug("处理DHT请求：{}", request.getQ());
		if(request.getQ() == null) {
			LOGGER.warn("处理DHT请求失败（类型不支持）：{}", request.getQ());
			response = Response.buildErrorResponse(request.getT(), ErrorCode.CODE_204.code(), "不支持的请求类型");
		} else {
			switch (request.getQ()) {
			case PING:
				response = ping(request);
				break;
			case FIND_NODE:
				response = findNode(request);
				break;
			case GET_PEERS:
				response = getPeers(request);
				break;
			case ANNOUNCE_PEER:
				response = announcePeer(request);
				break;
			default:
				LOGGER.info("处理DHT请求失败（类型未适配）：{}", request.getQ());
				response = Response.buildErrorResponse(request.getT(), ErrorCode.CODE_202.code(), "未适配的请求类型");
				break;
			}
		}
		pushMessage(response, socketAddress);
	}
	
	/**
	 * <p>处理响应</p>
	 * 
	 * @param response 响应
	 */
	private void onResponse(final Response response) {
		final Request request = DhtManager.getInstance().response(response);
		if(request == null) {
			LOGGER.warn("处理DHT响应失败：没有对应的请求");
			return;
		}
		LOGGER.debug("处理DHT响应：{}", request.getQ());
		if(request.getQ() == null) {
			LOGGER.warn("处理DHT响应失败（类型不支持）：{}", request.getQ());
			return;
		}
		if(!SUCCESS_VERIFY.apply(response)) {
			LOGGER.warn("处理DHT响应失败（失败响应）：{}", response);
			return;
		}
		switch (request.getQ()) {
		case PING:
			ping(request, response);
			break;
		case FIND_NODE:
			findNode(request, response);
			break;
		case GET_PEERS:
			getPeers(request, response);
			break;
		case ANNOUNCE_PEER:
			announcePeer(request, response);
			break;
		default:
			LOGGER.info("处理DHT响应失败（类型未适配）：{}", request.getQ());
			break;
		}
	}
	
	/**
	 * <p>发送请求：ping</p>
	 * <p>检测节点是否可达，该方法同步阻塞，收到响应后添加系统节点。</p>
	 * 
	 * @param socketAddress 地址
	 * 
	 * @return 节点信息
	 */
	public NodeSession ping(InetSocketAddress socketAddress) {
		LOGGER.debug("发送DHT请求：ping");
		final PingRequest request = PingRequest.newRequest();
		pushMessage(request, socketAddress);
		request.waitResponse();
		final Response response = RESPONSE_GETTER.apply(request);
		if(SUCCESS_VERIFY.apply(response)) {
			final NodeSession nodeSession = NodeManager.getInstance().newNodeSession(response.getNodeId(), socketAddress.getHostString(), socketAddress.getPort());
			NodeManager.getInstance().sortNodes();
			return nodeSession;
		}
		return null;
	}

	/**
	 * <p>处理请求：ping</p>
	 */
	private Response ping(Request request) {
		return PingRequest.execute(request);
	}

	/**
	 * <p>处理响应：ping</p>
	 */
	private void ping(Request request, Response response) {
		request.notifyResponse();
	}
	
	/**
	 * <p>发送请求：findNode</p>
	 * 
	 * @param socketAddress 地址
	 * @param target NodeId或者InfoHash
	 */
	public void findNode(InetSocketAddress socketAddress, byte[] target) {
		LOGGER.debug("发送DHT请求：findNode");
		final FindNodeRequest request = FindNodeRequest.newRequest(target);
		pushMessage(request, socketAddress);
	}
	
	/**
	 * <p>处理请求：findNode</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	private Response findNode(Request request) {
		return FindNodeRequest.execute(request);
	}
	
	/**
	 * <p>处理响应：findNode</p>
	 * 
	 * @param request 请求
	 * @param response 响应
	 */
	private void findNode(Request request, Response response) {
		FindNodeResponse.newInstance(response).getNodes();
	}

	/**
	 * <p>发送请求：getPeers</p>
	 * 
	 * @param socketAddress 地址
	 * @param infoHash InfoHash
	 */
	public void getPeers(InetSocketAddress socketAddress, byte[] infoHash) {
		LOGGER.debug("发送DHT请求：getPeers");
		final GetPeersRequest request = GetPeersRequest.newRequest(infoHash);
		pushMessage(request, socketAddress);
	}

	/**
	 * <p>处理请求：getPeers</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	private Response getPeers(Request request) {
		return GetPeersRequest.execute(request);
	}

	/**
	 * <p>处理响应：getPeers</p>
	 * <p>处理完成后发送声明消息</p>
	 * 
	 * @param request 请求
	 * @param response 响应
	 */
	private void getPeers(Request request, Response response) {
		final GetPeersResponse getPeersResponse = GetPeersResponse.newInstance(response);
		if(getPeersResponse.havePeers()) {
			getPeersResponse.getPeers(request);
		}
		if(getPeersResponse.haveNodes()) {
			getPeersResponse.getNodes();
		}
		// 发送声明消息
		final byte[] token = getPeersResponse.getToken();
		if(token != null) {
			final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
			final String infoHashHex = StringUtils.hex(infoHash);
			final TorrentSession torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
			if(torrentSession != null) {
				this.announcePeer(request.getSocketAddress(), token, infoHash);
			}
		}
	}
	
	/**
	 * <p>发送请求：announcePeer</p>
	 * 
	 * @param socketAddress 地址
	 * @param token Token
	 * @param infoHash InfoHash
	 */
	public void announcePeer(InetSocketAddress socketAddress, byte[] token, byte[] infoHash) {
		LOGGER.debug("发送DHT请求：announcePeer");
		final AnnouncePeerRequest request = AnnouncePeerRequest.newRequest(token, infoHash);
		pushMessage(request, socketAddress);
	}
	
	/**
	 * <p>处理请求：announcePeer</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	private Response announcePeer(Request request) {
		return AnnouncePeerRequest.execute(request);
	}

	/**
	 * <p>处理响应：announcePeer</p>
	 * 
	 * @param request 请求
	 * @param response 响应
	 */
	private void announcePeer(Request request, Response response) {
	}

	/**
	 * <p>发送请求</p>
	 * 
	 * @param request 请求
	 * @param socketAddress 地址
	 */
	private void pushMessage(Request request, InetSocketAddress socketAddress) {
		request.setSocketAddress(socketAddress);
		DhtManager.getInstance().put(request);
		final ByteBuffer buffer = ByteBuffer.wrap(request.toBytes());
		pushMessage(buffer, socketAddress);
	}
	
	/**
	 * <p>发送响应</p>
	 * 
	 * @param response 响应
	 * @param socketAddress 地址
	 */
	private void pushMessage(Response response, InetSocketAddress socketAddress) {
		if(response != null) {
			final ByteBuffer buffer = ByteBuffer.wrap(response.toBytes());
			pushMessage(buffer, socketAddress);
		}
	}
	
	/**
	 * <p>发送数据</p>
	 * 
	 * @param buffer 消息
	 * @param socketAddress 地址
	 */
	private void pushMessage(ByteBuffer buffer, InetSocketAddress socketAddress) {
		try {
			this.send(buffer, socketAddress);
		} catch (NetException e) {
			LOGGER.error("DHT消息发送异常", e);
		}
	}

}
