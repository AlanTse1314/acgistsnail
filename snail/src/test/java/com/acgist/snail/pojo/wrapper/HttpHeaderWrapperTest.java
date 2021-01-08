package com.acgist.snail.pojo.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.UnsupportedEncodingException;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.utils.Performance;

public class HttpHeaderWrapperTest extends Performance {

	@Test
	public void testFileName() throws NetException {
		final var header = HTTPClient.newInstance("https://g18.gdl.netease.com/MY-1.246.1.apk").head();
//		final var header = HTTPClient.newInstance("http://share.qiniu.easepan.xyz/tool/7tt_setup.exe").head();
//		final var header = HTTPClient.newInstance("https://g37.gdl.netease.com/onmyoji_setup_9.4.0.zip").head();
		this.log(header);
		final String defaultName = "test";
		final String fileName = header.fileName(defaultName);
		this.log(fileName);
		assertNotEquals(defaultName, fileName);
	}
	
	@Test
	public void testFileNameEx() throws UnsupportedEncodingException {
		var headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename='snail.jar'")), (a, b) -> true);
		var headerWrapper = HttpHeaderWrapper.newInstance(headers);
		var fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("snail.jar", fileName);
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename='%e6%b5%8b%e8%af%95.exe'")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("测试.exe", fileName);
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename='" + new String("测试.exe".getBytes("GBK"), "ISO-8859-1") + "'")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("测试.exe", fileName);
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename=\"snail.jar\"")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("snail.jar", fileName);
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename=\"￨ﾜﾗ￧ﾉﾛ.txt\"")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("蜗牛.txt", fileName);
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename=snail.jar?version=1.0.0")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("snail.jar", fileName);
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("inline;filename=\"snail.jar\";filename*=utf-8''snail.jar")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("snail.jar", fileName);
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("inline;charset=utf-8;fileName=\"snail.jar\";filename*=utf-8''snail.jar")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("snail.jar", fileName);
	}
	
}
