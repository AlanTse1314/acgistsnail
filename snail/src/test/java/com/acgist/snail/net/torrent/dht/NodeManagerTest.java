package com.acgist.snail.net.torrent.dht;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;

public class NodeManagerTest extends Performance {

	@Test
	public void testNewNodeSession() {
		LoggerConfig.off();
//		this.costed(1000, () -> {
//			NodeManager.getInstance().newNodeSession(StringUtils.unhex(buildId()), "0", 0);
//		});
//		this.costed(1000, () -> {
//			NodeManager.getInstance().sortNodes();
//		});
		this.costed(10000, () -> {
			NodeManager.getInstance().newNodeSession(StringUtils.unhex(buildId()), "0", 0);
		});
		var oldNodes = NodeManager.getInstance().nodes();
		var newNodes = NodeManager.getInstance().nodes();
		this.log(oldNodes.size());
		this.log(newNodes.size());
		Collections.sort(newNodes);
		assertTrue(oldNodes != newNodes);
		assertEquals(oldNodes.size(), newNodes.size());
//		oldNodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
//		newNodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
		for (int index = 0; index < oldNodes.size(); index++) {
			assertEquals(oldNodes.get(index), newNodes.get(index));
		}
	}

	@Test
	public void testFindNode() {
		LoggerConfig.off();
		this.costed(10000, () -> {
			NodeManager.getInstance().newNodeSession(StringUtils.unhex(buildId()), "0", 0);
		});
		long size = NodeManager.getInstance().nodes().stream().filter(NodeSession::persistentable).count();
		this.log("可用节点：{}", size);
		final var target = buildId();
//		final var target = StringUtils.hex(NodeManager.getInstance().nodes().get(0).getId());
		final var nodes = NodeManager.getInstance().findNode(target);
		nodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
		assertEquals(8, nodes.size());
		this.log(nodes.size());
		this.log(target);
		this.costed(10000, () -> NodeManager.getInstance().findNode(target));
//		this.costed(10000, 10, () -> NodeManager.getInstance().findNode(target));
		size = NodeManager.getInstance().nodes().stream().filter(NodeSession::persistentable).count();
		this.log("可用节点：{}", size);
	}

	@Test
	public void testMinFindNode() {
		LoggerConfig.off();
		this.costed(2, () -> {
			NodeManager.getInstance().newNodeSession(StringUtils.unhex(buildId()), "0", 0);
		});
		final var target = buildId();
		final var nodes = NodeManager.getInstance().findNode(target);
		nodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
		assertTrue(8 > nodes.size());
		this.log(nodes.size());
		this.log(target);
	}

	private String buildId() {
//		long value;
//		final Random random = new Random();
//		while((value = random.nextLong()) < 0);
//		return String.format("%040d", value);
		final byte[] bytes = new byte[20];
		final Random random = new Random();
		for (int index = 0; index < 20; index++) {
			bytes[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_MAX);
		}
		return StringUtils.hex(bytes);
	}
	
}
