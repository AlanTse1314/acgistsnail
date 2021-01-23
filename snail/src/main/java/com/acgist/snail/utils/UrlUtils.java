package com.acgist.snail.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.protocol.Protocol;

/**
 * <p>URL工具</p>
 * 
 * @author acgist
 */
public final class UrlUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(UrlUtils.class);
	
	private UrlUtils() {
	}
	
	/**
	 * <p>URL编码</p>
	 * 
	 * @param content 原始内容
	 * 
	 * @return 编码内容
	 */
	public static final String encode(String content) {
		if(StringUtils.isEmpty(content)) {
			return content;
		}
		try {
			return URLEncoder
				.encode(content, SystemConfig.DEFAULT_CHARSET)
				// 空格编码变成加号：加号解码变成空格
				.replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("URL编码异常：{}", content, e);
		}
		return content;
	}
	
	/**
	 * <p>URL解码</p>
	 * 
	 * @param content 编码内容
	 * 
	 * @return 原始内容
	 */
	public static final String decode(String content) {
		if(StringUtils.isEmpty(content)) {
			return content;
		}
		try {
			return URLDecoder.decode(content, SystemConfig.DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("URL解码异常：{}", content, e);
		}
		return content;
	}
	
	/**
	 * <p>获取跳转链接完整链接</p>
	 * <p>支持协议：HTTP、HTTPS</p>
	 * 
	 * @param source 原始页面链接
	 * @param target 目标页面链接
	 * 
	 * @return 完整链接
	 */
	public static final String redirect(final String source, String target) {
		Objects.requireNonNull(source, "原始页面链接不能为空");
		Objects.requireNonNull(target, "目标页面链接不能为空");
		target = target.trim();
		// 去掉引号
		if(target.startsWith(SymbolConfig.DOUBLE_QUOTE)) {
			target = target.substring(1);
		}
		if(target.endsWith(SymbolConfig.DOUBLE_QUOTE)) {
			target = target.substring(0, target.length() - 1);
		}
		if(Protocol.Type.HTTP.verify(target)) {
			// 完整链接
			return target;
		} else if(target.startsWith(SymbolConfig.SLASH)) {
			// 绝对目录链接
			final String prefix = Protocol.Type.HTTP.prefix(source);
			final int index = source.indexOf(SymbolConfig.SLASH_CHAR, prefix.length());
			if(index > prefix.length()) {
				return source.substring(0, index) + target;
			} else {
				return source + target;
			}
		} else {
			// 相对目录链接
			final String prefix = Protocol.Type.HTTP.prefix(source);
			final int index = source.lastIndexOf(SymbolConfig.SLASH_CHAR);
			if(index > prefix.length()) {
				return source.substring(0, index) + SymbolConfig.SLASH + target;
			} else {
				return source + SymbolConfig.SLASH + target;
			}
		}
	}
	
}
