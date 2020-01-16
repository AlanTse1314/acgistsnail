package com.acgist.snail.dht;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.dht.DhtClient;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.exception.DownloadException;

public class DhtServerTest extends BaseTest {

	private static final String HOST = "127.0.0.1";
	private static final int PORT = 18888; // 本地DHT测试端口
	
	private static final String HASH = "5E5324691812CAA0032EA76E813CCFC4D04E7E9E";
	
	@Test
	public void testPing() {
		DhtClient client = DhtClient.newInstance(HOST, PORT);
		NodeSession node = client.ping();
		this.log(node);
	}
	
	@Test
	public void testFindNode() {
		NodeManager.getInstance().newNodeSession("12345678901234567890".getBytes(), "192.168.1.100", 1234);
		NodeManager.getInstance().sortNodes();
		DhtClient client = DhtClient.newInstance(HOST, PORT);
		client.findNode(HASH);
		this.pause();
	}
	
	@Test
	public void testGetPeers() throws DownloadException {
		NodeManager.getInstance().newNodeSession("12345678901234567890".getBytes(), "192.168.1.100", 1234);
		NodeManager.getInstance().sortNodes();
		DhtClient client = DhtClient.newInstance(HOST, PORT);
		InfoHash infoHash = InfoHash.newInstance(HASH);
		client.getPeers(infoHash);
		this.pause();
	}
	
	@Test
	public void testAnnouncePeer() throws DownloadException {
		DhtClient client = DhtClient.newInstance(HOST, PORT);
		InfoHash infoHash = InfoHash.newInstance(HASH);
		client.announcePeer("1234".getBytes(), infoHash);
		this.pause();
	}
	
}
