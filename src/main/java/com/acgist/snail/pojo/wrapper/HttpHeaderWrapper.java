package com.acgist.snail.pojo.wrapper;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>HTTP头部信息信息包装器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class HttpHeaderWrapper extends HeaderWrapper {

	/**
	 * <p>断点续传：下载范围</p>
	 */
	private static final String CONTENT_RANGE = "Content-Range";
	/**
	 * <p>断点续传：范围请求</p>
	 */
	private static final String ACCEPT_RANGES = "Accept-Ranges";
	/**
	 * <p>下载大小</p>
	 */
	private static final String CONTENT_LENGTH = "Content-Length";
	/**
	 * <p>下载描述</p>
	 */
	private static final String CONTENT_DISPOSITION = "Content-Disposition";
	/**
	 * <p>范围请求：支持断点续传</p>
	 */
	private static final String BYTES = "bytes";
	/**
	 * <p>文件名称</p>
	 */
	private static final String FILENAME = "filename";
	
	private HttpHeaderWrapper(Map<String, List<String>> headers) {
		super(headers);
	}

	public static final HttpHeaderWrapper newInstance(HttpHeaders httpHeaders) {
		Map<String, List<String>> headers = null;
		if(httpHeaders != null) {
			headers = httpHeaders.map().entrySet().stream()
				.filter(entry -> CollectionUtils.isNotEmpty(entry.getValue()))
				.collect(Collectors.toMap(
					entry -> entry.getKey(),
					entry -> entry.getValue()
				));
		}
		return new HttpHeaderWrapper(headers);
	}
	
	/**
	 * <p>获取文件名称</p>
	 * <p>下载文件名称：如果不存在返回默认的文件名称</p>
	 * <p>Content-Disposition:attachment;filename=snail.jar?version=1.0.0</p>
	 * 
	 * @param defaultName 默认文件名称
	 */
	public String fileName(final String defaultName) {
		String fileName = header(CONTENT_DISPOSITION);
		if(StringUtils.isEmpty(fileName)) {
			return defaultName;
		}
		final String fileNameLower = fileName.toLowerCase();
		if(fileNameLower.contains(FILENAME)) { // 包含文件名称
			fileName = UrlUtils.decode(fileName); // URL解码
			int index = fileName.indexOf("=");
			if(index != -1) {
				fileName = fileName.substring(index + 1);
				index = fileName.indexOf("?");
				if(index != -1) {
					fileName = fileName.substring(0, index);
				}
			}
			fileName = fileName.trim();
			if(StringUtils.isEmpty(fileName)) {
				return defaultName;
			}
			return fileName;
		} else {
			return defaultName;
		}
	}
	
	/**
	 * <p>获取文件大小</p>
	 * <p>Content-Length：102400</p>
	 */
	public long fileSize() {
		long size = 0L;
		final String value = header(CONTENT_LENGTH);
		if(value != null) {
			if(StringUtils.isNumeric(value)) {
				size = Long.parseLong(value);
			}
		}
		return size;
	}
	
	/**
	 * <dl>
	 * 	<dt>是否支持断点续传</dt>
	 * 	<dd>accept-ranges=bytes</dd>
	 * 	<dd>content-range=bytes 0-100/100</dd>
	 * </dl>
	 */
	public boolean range() {
		boolean range = false;
		final String acceptRanges = header(ACCEPT_RANGES);
		final String contentRange = header(CONTENT_RANGE);
		if(acceptRanges != null) {
			range = BYTES.equalsIgnoreCase(acceptRanges);
		}
		if(contentRange != null) {
			range = true;
		}
		return range;
	}

	/**
	 * <p>获取开始下载位置</p>
	 * <p>content-range=bytes 0-100/100</p>
	 */
	public long beginRange() {
		long range = 0L;
		final String contentRange = header(CONTENT_RANGE);
		if(contentRange != null) {
			final int endIndex = contentRange.lastIndexOf("-");
			final String value = contentRange.substring(5, endIndex).trim();
			if(StringUtils.isNumeric(value)) {
				range = Long.parseLong(value);
			}
		}
		return range;
	}
	
}
