package com.acgist.snail;

import java.io.File;
import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.pojo.bean.M3u8;
import com.acgist.snail.protocol.hls.bootstrap.M3u8Builder;
import com.acgist.snail.system.exception.DownloadException;

public class M3u8Test extends BaseTest {

//	http://ivi.bupt.edu.cn/hls/cctv6hd.m3u8
//	https://dco4urblvsasc.cloudfront.net/811/81095_ywfZjAuP/game/index.m3u8
	
	@Test
	public void build() throws DownloadException, MalformedURLException {
		var builder = M3u8Builder.newInstance(new File("E://snail/index.m3u8"), "https://www.acgist.com/a/b?v=1234");
		M3u8 m3u8 = builder.build();
		this.log(m3u8);
		builder = M3u8Builder.newInstance(new File("E://snail/cctv6hd.m3u8"), "https://www.acgist.com/a/b?v=1234");
		m3u8 = builder.build();
		this.log(m3u8);
	}
	
}
