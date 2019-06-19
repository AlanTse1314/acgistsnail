package com.acgist.snail.net.bt.peer.bootstrap.ltep;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.PeerConfig.ExtensionType;
import com.acgist.snail.system.manager.PeerManager;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.PeerUtils;

/**
 * <p>Peer Exchange (PEX)</p>
 * <p>Peer交换。在优化PeerClient后获取有效的Peer发送此消息。</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0011.html</p>
 * TODO：IPv6支持
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerExchangeMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerExchangeMessageHandler.class);
	
	public static final String ADDED = "added";
	public static final String ADDEDF = "added.f";
//	public static final String ADDED6 = "added6";
//	public static final String ADDED6F = "added6.f";
//	public static final String DROPPED = "dropped";
//	public static final String DROPPED6 = "dropped6";
	
	private final PeerSession peerSession;
	private final TorrentSession torrentSession;
	private final ExtensionMessageHandler extensionMessageHandler;
	
	public static final PeerExchangeMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		return new PeerExchangeMessageHandler(peerSession, torrentSession, extensionMessageHandler);
	}
	
	private PeerExchangeMessageHandler(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.extensionMessageHandler = extensionMessageHandler;
	}
	
	public void onMessage(ByteBuffer buffer) {
		exchange(buffer);
	}

	/**
	 * 发送请求
	 */
	public void exchange(byte[] bytes) {
		final Byte type = utPeerExchangeType(); // 扩展消息类型
		if (type == null) {
			LOGGER.warn("不支持UtPeerExchange扩展协议");
			return;
		}
		LOGGER.debug("发送Pex消息");
		this.extensionMessageHandler.pushMessage(type, bytes);
	}
	
	/**
	 * 处理请求，将获取的Peer加入到列表。
	 */
	private void exchange(ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
		final Map<String, Object> map = decoder.nextMap();
		if(map == null) {
			LOGGER.warn("UtPeerExchange消息格式错误：{}", decoder.obbString());
			return;
		}
		final byte[] added = decoder.getBytes(ADDED);
		final byte[] addedf = decoder.getBytes(ADDEDF);
		final var peers = PeerUtils.read(added);
		if(CollectionUtils.isNotEmpty(peers)) {
			final AtomicInteger index = new AtomicInteger(0);
			peers.forEach((host, port) -> {
				final PeerSession peerSession = PeerManager.getInstance().newPeerSession(
					this.torrentSession.infoHashHex(),
					this.torrentSession.statistics(),
					host,
					port,
					PeerConfig.SOURCE_PEX);
				if(addedf != null && addedf.length > index.get()) {
					peerSession.exchange(addedf[index.getAndIncrement()]);
				}
			});
		}
	}
	
	/**
	 * 客户端的消息类型
	 */
	private Byte utPeerExchangeType() {
		return this.peerSession.extensionTypeValue(ExtensionType.ut_pex);
	}
	
	public static final byte[] buildMessage(List<PeerSession> optimize) {
		if(CollectionUtils.isEmpty(optimize)) {
			return null;
		}
		final Map<String, Object> data = new HashMap<>();
		final int length = 6 * optimize.size();
		final ByteBuffer addedBuffer = ByteBuffer.allocate(length);
		final ByteBuffer addedfBuffer = ByteBuffer.allocate(optimize.size());
		optimize.forEach(session -> {
			addedBuffer.putInt(NetUtils.encodeIpToInt(session.host()));
			addedBuffer.putShort(NetUtils.encodePort(session.peerPort()));
			addedfBuffer.put(session.exchange());
		});
		data.put(ADDED, addedBuffer.array());
		data.put(ADDEDF, addedfBuffer.array());
		return BCodeEncoder.encodeMap(data);
	}

}
