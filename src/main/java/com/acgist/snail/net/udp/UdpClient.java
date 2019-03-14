package com.acgist.snail.net.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class UdpClient {

	public static void main(String[] args) throws IOException {
		new UdpClient().connect();
	}
	
	/**
	 */
	public void connect() throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		channel.bind(new InetSocketAddress(8888));
		channel.configureBlocking(false);
		Selector selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ);
//		channel.connect(new InetSocketAddress("localhost", 28888));
//		channel.connect(new InetSocketAddress("open.demonii.com", 1337));
		ByteBuffer buf = null;
		System.out.println("xx");
//		int x = channel.write(buf);
//		System.out.println(x);
		ByteBuffer bBuffer = ByteBuffer.allocate(1024);
		bBuffer.putLong(1234567890L); // connection_id
		bBuffer.putInt(1);
		bBuffer.putInt(12345);
		System.out.println("xxxx");
		channel.send(bBuffer, new InetSocketAddress("exodus.desync.com", 6969));
		bBuffer = ByteBuffer.allocate(1024);
		bBuffer.putLong(1234567890L); // connection_id
		bBuffer.putInt(0);
		bBuffer.putInt(12345);
		bBuffer.put("12345678901234567890".getBytes());// <<<< what you asked.. adding the infoHash which is byte[]
		bBuffer.put("12345678901234567890".getBytes());
		bBuffer.putLong(0L);//download
		bBuffer.putLong(0L);//left
		bBuffer.putLong(0L);//uploaded
		bBuffer.putInt(1);//event
		bBuffer.putInt(0);// local ip
		bBuffer.putInt(0);//secret key
		bBuffer.putInt(50);//numwant
		bBuffer.putShort((short)8888);//numwant
		bBuffer.putShort((short)0);
//		channel.write(buf);
//		System.out.println(new String(bBuffer.array()));
		System.out.println("xxxx");
		channel.send(bBuffer, new InetSocketAddress("exodus.desync.com", 6969));
//		int bytesWritten = channel.write(buf);
//		buf = ByteBuffer.allocate(1024);
//		byte[] bytes = baos.toByteArray();
//		System.out.println(new String(bytes));
//		channel.socket().send(new DatagramPacket(bytes, bytes.length));
//		System.out.println("xx" + bytesWritten);
//		channel.send(buf, new InetSocketAddress("localhost", 28888));
		System.out.println("00");
//		SocketAddress bytesRead = channel.receive(buf);
//		System.out.println("xx" + bytesRead);
//		System.out.println(new String(buf.array()));
		while(true) {
			final int keysSelected = selector.select();
			if(keysSelected > 0) {
				System.out.println("vvv");
				final Set<SelectionKey> selectedKeys = selector.selectedKeys();
				final Iterator<SelectionKey> selectedKeysIterator = selectedKeys.iterator();
				System.out.println("xxxx");
				while(selectedKeysIterator.hasNext()) {
					final SelectionKey selectedKey = selectedKeysIterator.next();
					selectedKeysIterator.remove();
					
					if(selectedKey.isValid() && selectedKey.isReadable()) {
						buf = ByteBuffer.allocate(1024);
						SocketAddress bytesRead = channel.receive(buf);
						System.out.println("xddx" + bytesRead);
						System.out.println(new String(buf.array()));
						buf.clear();
					}
				}
			} else {
				System.out.println("ccc");
			}
		}
	}
	
}
