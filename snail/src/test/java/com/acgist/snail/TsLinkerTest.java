package com.acgist.snail;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.hls.bootstrap.TsLinker;

public class TsLinkerTest extends BaseTest {

	@Test
	public void link() {
		final File parent = new File("E:/gitee/snail/snail-javafx/download/index.m3u8");
		final var links = List.of(parent.listFiles()).stream()
			.map(file -> file.getAbsolutePath())
			.collect(Collectors.toList());
		final TsLinker linker = TsLinker.newInstance(
			"index",
			"E:/gitee/snail/snail-javafx/download/index.m3u8",
			links
		);
		linker.link();
	}
	
}
