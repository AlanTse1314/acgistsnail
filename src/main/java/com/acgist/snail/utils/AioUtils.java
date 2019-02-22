package com.acgist.snail.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.config.SystemConfig;

/**
 * AIO工具
 */
public class AioUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(AioUtils.class);
	
	/**
	 * 读取内容
	 */
	public static final String readContent(ByteBuffer attachment) {
		CharsetDecoder decoder = Charset.forName(SystemConfig.DEFAULT_CHARSET).newDecoder();
//		decoder.onMalformedInput(CodingErrorAction.IGNORE);
		String content = null;
		try {
			attachment.flip();
			content = decoder.decode(attachment).toString();
			attachment.compact();
		} catch (CharacterCodingException e) {
			LOGGER.error("ByteBuffer解码异常", e);
		}
		return content;
	}
	
	/**
	 * 关闭socket
	 */
	public static final void close(
		AsynchronousChannelGroup group,
		AsynchronousServerSocketChannel server,
		AsynchronousSocketChannel socket
	) {
		if(socket != null && socket.isOpen()) {
			try {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			} catch (IOException e) {
				LOGGER.error("关闭Socket异常", e);
			}
		}
		if(server != null && server.isOpen()) {
			try {
				server.close();
			} catch (IOException e) {
				LOGGER.error("关闭Socket Server异常");
			}
		}
		if(group != null && !group.isShutdown()) {
			group.shutdown();
		}
	}
	
}
