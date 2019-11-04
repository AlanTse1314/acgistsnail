package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.PeerConfig.ExtensionType;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.exception.PacketSizeException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.PeerUtils;

/**
 * <p>Peer Exchange (PEX)</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0011.html</p>
 * <p>Peer交换：在优化PeerLauncher后获取有效的Peer发送此消息</p>
 * 
 * TODO：IPv6
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerExchangeMessageHandler extends ExtensionTypeMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerExchangeMessageHandler.class);
	
	//================IPv4================//
	private static final String ADDED = "added";
	private static final String ADDEDF = "added.f";
	private static final String DROPPED = "dropped";
	//================IPv6================//
	private static final String ADDED6 = "added6";
	private static final String ADDED6F = "added6.f";
	private static final String DROPPED6 = "dropped6";
	
	private final TorrentSession torrentSession;
	
	private PeerExchangeMessageHandler(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		super(ExtensionType.UT_PEX, peerSession, extensionMessageHandler);
		this.torrentSession = torrentSession;
	}
	
	public static final PeerExchangeMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		return new PeerExchangeMessageHandler(peerSession, torrentSession, extensionMessageHandler);
	}
	
	@Override
	public void doMessage(ByteBuffer buffer) throws NetException {
		pex(buffer);
	}
	
	/**
	 * 发送消息：pex
	 */
	public void pex(byte[] bytes) {
		LOGGER.debug("发送pex消息");
		this.pushMessage(bytes);
	}
	
	/**
	 * <p>处理消息：pex</p>
	 * 
	 * TODO：IPv6
	 */
	private void pex(ByteBuffer buffer) throws PacketSizeException {
		LOGGER.debug("处理pex消息");
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final var decoder = BEncodeDecoder.newInstance(bytes);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("处理pex消息错误（格式）：{}", decoder.oddString());
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
					peerSession.flags(addedf[index.getAndIncrement()]);
				}
			});
		}
	}
	
	/**
	 * 创建pex消息
	 * 
	 * TODO：IPv6
	 */
	public static final byte[] buildMessage(List<PeerSession> optimize) {
		if(CollectionUtils.isEmpty(optimize)) {
			return null;
		}
		final int length = 6 * optimize.size();
		final ByteBuffer addedBuffer = ByteBuffer.allocate(length);
		final ByteBuffer addedfBuffer = ByteBuffer.allocate(optimize.size());
		optimize.stream()
			.distinct()
			.forEach(session -> {
				addedBuffer.putInt(NetUtils.encodeIpToInt(session.host()));
				addedBuffer.putShort(NetUtils.encodePort(session.port()));
				addedfBuffer.put(session.flags());
			});
		final Map<String, Object> data = new HashMap<>(6);
		final byte[] emptyBytes = new byte[0];
		data.put(ADDED, addedBuffer.array());
		data.put(ADDEDF, addedfBuffer.array());
		data.put(DROPPED, emptyBytes);
		data.put(ADDED6, emptyBytes);
		data.put(ADDED6F, emptyBytes);
		data.put(DROPPED6, emptyBytes);
		return BEncodeEncoder.encodeMap(data);
	}

}
