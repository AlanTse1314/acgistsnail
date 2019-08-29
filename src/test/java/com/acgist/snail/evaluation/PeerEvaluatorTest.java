package com.acgist.snail.evaluation;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.acgist.snail.repository.DatabaseManager;
import com.acgist.snail.repository.impl.ConfigRepository;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.evaluation.PeerEvaluator;
import com.acgist.snail.utils.NetUtils;

public class PeerEvaluatorTest {

	@Test
	public void merge() {
		ConfigRepository repository = new ConfigRepository();
		Map<Integer, Long> map = new HashMap<>();
		for (int index = 0; index < 100; index++) {
			map.put(index, (long) index);
		}
		repository.mergeConfig("acgist.system.range", new String(BEncodeEncoder.encodeMap(map)));
	}
	
	@Test
	public void delete() {
		DatabaseManager.getInstance();
		ConfigRepository repository = new ConfigRepository();
		boolean delete = repository.deleteName("acgist.system.range");
		System.out.println("删除结果：" + delete);
	}
	
	@Test
	public void load() {
		PeerEvaluator.getInstance().init();
		long begin = System.currentTimeMillis();
		var map = PeerEvaluator.getInstance().ranges();
		if(map != null) {
			map.entrySet().stream()
			.sorted((a, b) -> {
//				return a.getKey().compareTo(b.getKey()); // IP段
				return a.getValue().compareTo(b.getValue()); // 评分
			})
			.forEach(entry -> {
				System.out.print(String.format("%05d", entry.getKey()) + "=" + entry.getValue());
				System.out.print("-");
				System.out.println(NetUtils.decodeLongToIp(1L * (2 << 15) * entry.getKey()));
			});
			System.out.println("数量：" + map.size());
		} else {
			System.out.println("--");
		}
		long end = System.currentTimeMillis();
		System.out.println(end - begin);
	}
	
}
