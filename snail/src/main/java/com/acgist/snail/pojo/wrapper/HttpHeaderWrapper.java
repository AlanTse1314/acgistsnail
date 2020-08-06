package com.acgist.snail.pojo.wrapper;

import java.net.http.HttpHeaders;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>HTTP头部信息包装器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class HttpHeaderWrapper extends HeaderWrapper {

	/**
	 * <p>服务器名称：{@value}</p>
	 */
	public static final String HEADER_SERVER = "Server";
	/**
	 * <p>MIME类型：{@value}</p>
	 */
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	/**
	 * <p>接收范围请求：{@value}</p>
	 * <p>没有接收{@link #HEADER_RANGE}时返回全部数据</p>
	 */
	public static final String HEADER_ACCEPT_RANGES = "Accept-Ranges";
	/**
	 * <p>请求下载范围：{@value}</p>
	 * <p>接收{@link #HEADER_RANGE}时返回范围数据</p>
	 */
	public static final String HEADER_CONTENT_RANGE = "Content-Range";
	/**
	 * <p>下载大小：{@value}</p>
	 */
	public static final String HEADER_CONTENT_LENGTH = "Content-Length";
	/**
	 * <p>下载描述：{@value}</p>
	 */
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	/**
	 * <p>范围请求：{@value}</p>
	 * <table border="1">
	 * 	<caption>HTTP协议断点续传设置</caption>
	 * 	<tr>
	 * 		<td>{@code Range: bytes=0-499}</td>
	 * 		<td>{@code 0}-{@code 499}字节范围</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Range: bytes=500-999}</td>
	 * 		<td>{@code 500}-{@code 999}字节范围</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Range: bytes=-500}</td>
	 * 		<td>最后{@code 500}字节</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Range: bytes=500-}</td>
	 * 		<td>{@code 500}字节开始到结束</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Range: bytes=0-0,-1}</td>
	 * 		<td>第一个字节和最后一个字节</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Range: bytes=500-600,601-999}</td>
	 * 		<td>同时指定多个范围</td>
	 * 	</tr>
	 * </table>
	 */
	public static final String HEADER_RANGE = "Range";
	/**
	 * <p>接收范围请求：{@value}</p>
	 * 
	 * @see #HEADER_ACCEPT_RANGES
	 */
	public static final String HEADER_BYTES = "bytes";
	/**
	 * <p>文件名称：{@value}</p>
	 * 
	 * @see #HEADER_CONTENT_DISPOSITION
	 */
	public static final String HEADER_FILENAME = "filename";
	
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
	 * <pre>
	 * Content-Disposition:attachment;filename="snail.jar"
	 * Content-Disposition:attachment;filename=snail.jar?version=1.0.0
	 * Content-Disposition:inline;filename="snail.jar";filename*=utf-8''snail.jar
	 * </pre>
	 * 
	 * @param defaultName 默认文件名称
	 * 
	 * @return 文件名称
	 */
	public String fileName(final String defaultName) {
		String fileName = header(HEADER_CONTENT_DISPOSITION);
		if(StringUtils.isEmpty(fileName)) {
			return defaultName;
		}
		final String fileNameLower = fileName.toLowerCase();
		if(fileNameLower.contains(HEADER_FILENAME)) { // 包含文件名称
			fileName = extractFileName(fileName);
			return charsetFileName(fileName, defaultName);
		} else {
			return defaultName;
		}
	}

	/**
	 * <p>头部信息名称提取</p>
	 * 
	 * @param fileName 文件名称
	 * 
	 * @return 文件名称
	 */
	private String extractFileName(String fileName) {
		int index;
		// URL解码
		fileName = UrlUtils.decode(fileName);
		// 删除：filename前面内容
		index = fileName.indexOf(HEADER_FILENAME);
		if(index != -1) {
			fileName = fileName.substring(index + HEADER_FILENAME.length());
		}
		// 删除：等号前面内容
		index = fileName.indexOf('=');
		if(index != -1) {
			fileName = fileName.substring(index + 1);
		}
		// 删除：分号后面内容
		index = fileName.indexOf(';');
		if(index != -1) {
			fileName = fileName.substring(0, index);
		}
		// 删除：空格
		fileName = fileName.trim();
		// 删除：引号前后内容
		if(fileName.startsWith("\'") && fileName.endsWith("\'")) {
			fileName = fileName.substring(1, fileName.length() - 1);
		}
		if(fileName.startsWith("\"") && fileName.endsWith("\"")) {
			fileName = fileName.substring(1, fileName.length() - 1);
		}
		// 删除：参数
		index = fileName.indexOf('?');
		if(index != -1) {
			fileName = fileName.substring(0, index);
		}
		// 删除：空格
		return fileName.trim();
	}
	
	/**
	 * <p>文件名称解码</p>
	 * 
	 * @param fileName 文件名称
	 * @param defaultName 默认文件名称
	 * 
	 * @return 文件名称
	 */
	private String charsetFileName(String fileName, final String defaultName) {
		if(StringUtils.isEmpty(fileName)) {
			return defaultName;
		}
		final var gbkEncoder = Charset.forName(SystemConfig.CHARSET_GBK).newEncoder();
		// 只是进行URL编码
		if(gbkEncoder.canEncode(fileName)) {
			return fileName;
		}
		// HttpClient工具汉字ISO-8859-1字符转为char没有去掉符号（& 0xFF）
		final char[] chars = fileName.toCharArray();
		for (int index = 0; index < chars.length; index++) {
			// 去掉符号
			chars[index] = (char) (chars[index] & 0x00FF);
		}
		fileName = new String(chars);
		// 处理ISO-8859-1编码
		// GBK
		final String fileNameGBK = StringUtils.charset(fileName, SystemConfig.CHARSET_ISO_8859_1, SystemConfig.CHARSET_GBK);
		// UTF-8
		final String fileNameUTF8 = StringUtils.charsetFrom(fileName, SystemConfig.CHARSET_ISO_8859_1);
		/*
		 * <p>判断依据</p>
		 * <p>GBK转为UTF8基本乱码</p>
		 * <p>UTF8转为GBK也会乱码，可能只是不是原来的字符，但是可以也是属于GBK字符。</p>
		 */
		if(gbkEncoder.canEncode(fileNameUTF8)) {
			return fileNameUTF8;
		}
		if(gbkEncoder.canEncode(fileNameGBK)) {
			return fileNameGBK;
		}
		// 其他编码直接返回
		return fileName;
	}
	
	/**
	 * <p>获取文件大小</p>
	 * <p>Content-Length：102400</p>
	 * 
	 * @return 文件大小
	 */
	public long fileSize() {
		long size = 0L;
		final String value = header(HEADER_CONTENT_LENGTH);
		if(value != null) {
			if(StringUtils.isNumeric(value)) {
				size = Long.parseLong(value);
			}
		}
		return size;
	}
	
	/**
	 * <p>判断是否支持断点续传</p>
	 * <p>Accept-Ranges=bytes</p>
	 * <p>Content-Range=bytes 0-100/100</p>
	 * 
	 * @return 是否支持断点续传
	 */
	public boolean range() {
		boolean range = false;
		final String acceptRanges = header(HEADER_ACCEPT_RANGES);
		final String contentRange = header(HEADER_CONTENT_RANGE);
		if(acceptRanges != null) {
			range = HEADER_BYTES.equalsIgnoreCase(acceptRanges);
		}
		if(contentRange != null) {
			range = true;
		}
		return range;
	}

	/**
	 * <p>获取开始下载位置</p>
	 * <p>Content-Range=bytes 0-100/100</p>
	 * 
	 * @return 开始下载位置
	 */
	public long beginRange() {
		long range = 0L;
		final String contentRange = header(HEADER_CONTENT_RANGE);
		if(contentRange != null) {
			final int endIndex = contentRange.lastIndexOf('-');
			final String value = contentRange.substring(HEADER_BYTES.length(), endIndex).trim();
			if(StringUtils.isNumeric(value)) {
				range = Long.parseLong(value);
			}
		}
		return range;
	}
	
}
