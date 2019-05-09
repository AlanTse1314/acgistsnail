package com.acgist.main;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.utils.StringUtils;

public class NodeManagerTest {

	@Test
	public void xor() {
		String hex1 = NodeManager.xor(StringUtils.unhex("c15419ae6b3bdfd8e983062b0650ad114ce41859"), StringUtils.unhex("c15417e6aeab33732a59085d826edd29978f9afa"));
		String hex2 = NodeManager.xor(StringUtils.unhex("c1540515408feb76af06c6c588b1b345b5173c42"), StringUtils.unhex("c15417e6aeab33732a59085d826edd29978f9afa"));
		System.out.println(hex1);
		System.out.println(hex2);
	}
	
	@Test
	public void findNode() {
		List<NodeSession> nodes = new ArrayList<>();
		for (int index = 100000; index < 110000; index++) {
			nodes.add(NodeManager.getInstance().newNodeSession(StringUtils.unhex("1111111111111111111111111111111111" + index), "0", 0));
		}
		System.out.println(nodes.contains(NodeManager.getInstance().newNodeSession(StringUtils.unhex("1111111111111111111111111111111111102022"), "0", 0)));
		var list = NodeManager.getInstance().findNode("1111111111111111111111111111111111112023");
		list.forEach(node -> {
			System.out.println(node);
		});
	}
	
}
