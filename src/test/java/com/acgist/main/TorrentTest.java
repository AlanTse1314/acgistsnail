package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.system.manager.TorrentSessionManager;

public class TorrentTest {

	@Test
	public void test() throws Exception {
//		String path = "e:/snail/5b293c290c78c503bcd59bc0fbf78fd213ce21a4.torrent";
		String path = "e:/snail/82309348090ecbec8bf509b83b30b78a8d1f6454.torrent";
//		String path = "e:/snail/9d3854d8c6280049e5d85e490ff07de7c2bd96a2.torrent";
//		String path = "e:/snail/868f1199b18d05bf103aa8a8321f6428854d712e.torrent";
//		String path = "e:/snail/11e38a5270e15c60534ca48977b7d77a3c4f6340.torrent";
//		String path = "e:/snail/0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent";
		TorrentSession session = TorrentSessionManager.getInstance().buildSession(path);
		Torrent torrent = session.torrent();
		System.out.println(torrent.getComment());
		System.out.println("种子HASH：" + session.infoHash().hashHex());
//		System.out.println(new Date(torrent.getCreationDate() * 1000));
//		System.out.println(torrent.getCreationDate());
//		System.out.println(torrent.getEncoding());
//		System.out.println(torrent.getCreateBy());
//		System.out.println(torrent.getInfo().getLength());
		System.out.println(torrent.getInfo().getPieceLength());
		System.out.println(torrent.getInfo().getPieces().length);
		System.out.println(torrent.getAnnounce());
		if (torrent.getAnnounceList().size() > 0) {
			for (String tmp : torrent.getAnnounceList()) {
				System.out.println(tmp);
			}
		}
//		System.out.println(torrent.getNodes());
//		TorrentInfo torrentInfo = torrent.getInfo();
//		System.out.println(torrentInfo.getName());
//		System.out.println(torrentInfo.getLength());
//		System.out.println(torrentInfo.getPieceLength());
//		System.out.println(torrentInfo.getPieces());
//		System.out.println(torrentInfo.ed2kHex());
//		System.out.println(torrentInfo.filehashHex());
//		if (torrentInfo.getFiles().size() > 0) {
//			for (TorrentFile file : torrentInfo.getFiles()) {
//				System.out.println("----------------file----------------");
//				System.out.println("文件长度：" + file.getLength());
//				System.out.println("ed2k：" + StringUtils.hex(file.getEd2k()));
//				System.out.println("filehash：" + StringUtils.hex(file.getFilehash()));
//				if (file.getPath().size() > 0) {
//					System.out.println("文件路径：" + String.join("/", file.getPath()));
//					System.out.println("文件路径：" + String.join("/", file.getPathUtf8()));
//				}
//			}
//		}
//		System.out.println(JsonUtils.toJson(torrent));
	}
	
}
