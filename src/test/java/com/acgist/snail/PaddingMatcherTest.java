package com.acgist.snail;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.net.torrent.peer.bootstrap.crypt.MSEPaddingSync;

public class PaddingMatcherTest {

	@Test
	public void cos() {
		long begin = System.currentTimeMillis();
		for (int index = 0; index < 100000; index++) {
			sync();
		}
		System.out.println(System.currentTimeMillis() - begin);
	}
	
	@Test
	public void sync() {
		MSEPaddingSync sync = MSEPaddingSync.newInstance(3);
		ByteBuffer buffer = ByteBuffer.allocate(100);
		buffer.putShort((short) 2).put("12".getBytes());
//		buffer.putShort((short) 0);
		buffer.putShort((short) 100);
		buffer.put("UU34".getBytes());
		buffer.flip();
		
		boolean ok = sync.sync(buffer);
		System.out.println(ok);
		System.out.println(sync);
		
		ByteBuffer append = ByteBuffer.allocate(98);
		append.put("0".repeat(96).getBytes());
		append.putShort((short) 0);
		append.flip();
		
		ok = sync.sync(append);
		System.out.println(ok);
		System.out.println(sync);
		
//		ByteBuffer append = ByteBuffer.allocate(96);
//		append.put("0".repeat(96).getBytes());
//		append.flip();
//		
//		ok = sync.sync(append);
//		System.out.println(ok);
//		System.out.println(sync);
//		
//		ByteBuffer zero = ByteBuffer.allocate(2);
//		zero.putShort((short) 0);
//		zero.flip();
//		
//		ok = sync.sync(zero);
//		System.out.println(ok);
//		System.out.println(sync);
		
		sync.allPadding().forEach(bytes -> {
			System.out.println("内容：" + new String(bytes));
		});
	}
	
}
