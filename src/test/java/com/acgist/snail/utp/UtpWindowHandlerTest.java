package com.acgist.snail.utp;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.net.utp.bootstrap.UtpWindowData;
import com.acgist.snail.net.utp.bootstrap.UtpWindowHandler;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class UtpWindowHandlerTest {

	@Test
	public void test() throws NetException {
		UtpWindowHandler handler = UtpWindowHandler.newInstance();
		long begin = System.currentTimeMillis();
		for (int i = 1; i < 100000; i++) {
			UtpWindowData windowData = handler.receive(0, (short) i, ByteBuffer.allocate(10));
			if(windowData == null) {
				System.out.println(i);
			}
		}
		System.out.println(System.currentTimeMillis() - begin);
	}

	@Test
	public void timeout() {
		UtpWindowHandler handler = UtpWindowHandler.newInstance();
		handler.send("1234".getBytes());
		for (int i = 0; i < 100; i++) {
			System.out.println(handler.timeoutWindowData());
			ThreadUtils.sleep(1000);
		}
	}
	
}
