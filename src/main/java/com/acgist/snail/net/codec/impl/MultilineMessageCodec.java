package com.acgist.snail.net.codec.impl;

import java.net.InetSocketAddress;

import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>多行消息处理器</p>
 * <p>必须配合{@linkplain LineMessageCodec 行消息处理器}一起使用</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public final class MultilineMessageCodec extends MessageCodec<String, String> {

	/**
	 * <p>换行符</p>
	 */
	private final String split;
	/**
	 * <p>多行结束符（正则表达式）</p>
	 */
	private final String endRegex;
	/**
	 * <p>多行消息</p>
	 */
	private final StringBuilder message;
	
	public MultilineMessageCodec(IMessageCodec<String> messageCodec, String split, String endRegex) {
		super(messageCodec);
		this.split = split;
		this.endRegex = endRegex;
		this.message = new StringBuilder();
	}

	@Override
	protected void decode(String message, InetSocketAddress address, boolean haveAddress) throws NetException {
		if(StringUtils.regex(message, this.endRegex, false)) {
			this.message.append(message);
			this.doNext(this.message.toString(), address, haveAddress);
			this.message.setLength(0);
		} else {
			this.message.append(message).append(this.split);
		}
	}

}
