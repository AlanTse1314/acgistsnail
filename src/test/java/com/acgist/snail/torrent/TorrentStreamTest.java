package com.acgist.snail.torrent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

public class TorrentStreamTest extends BaseTest {

	@Test
	public void read() throws IOException {
		RandomAccessFile stream = new RandomAccessFile("e:/tmp.txt", "rwd");
		stream.write(2);
		stream.seek(1000);
		byte[] bytes = new byte[1024];
		stream.read(bytes);
		this.log(stream.length());
		this.pause();
		stream.close();
	}
	
	@Test
	public void randomAccessFile() throws IOException {
		RandomAccessFile file = new RandomAccessFile("F:/迅雷下载/我的大叔/[我的大叔][E001].mkv", "rw");
		long begin = System.currentTimeMillis();
		byte[] bytes = new byte[1024 * 1024];
		for (int index = 0; index < 1024; index++) {
			file.seek(200 * 1024 * 1024 + index);
			file.read(bytes, 0, bytes.length);
			StringUtils.sha1(bytes);
		}
		long end = System.currentTimeMillis();
		this.log(end - begin);
		file.close();
	}
	
	@Test
	public void fileChannel() throws IOException {
		FileChannel file = FileChannel.open(Paths.get("F:/迅雷下载/我的大叔/[我的大叔][E001].mkv"), StandardOpenOption.READ, StandardOpenOption.WRITE);
		long begin = System.currentTimeMillis();
		ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
		for (int index = 0; index < 1024; index++) {
			file.position(200 * 1024 * 1024 + index);
			file.read(buffer);
			buffer.flip();
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			StringUtils.sha1(bytes);
		}
		long end = System.currentTimeMillis();
		this.log(end - begin);
		file.close();
	}
	
	@Test
	public void writeRead() throws Exception {
		byte[] bytes = Files.readAllBytes(Path.of("F:/壁纸/01.png"));
		var channel = FileChannel.open(Path.of("F:/TEST.png"), StandardOpenOption.READ, StandardOpenOption.WRITE);
		channel.write(ByteBuffer.wrap(bytes));
//		channel.force(true);
//		channel.force(false);
		ThreadUtils.sleep(100000);
	}
	
}
