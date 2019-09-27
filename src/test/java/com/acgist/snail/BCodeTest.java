package com.acgist.snail;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.exception.OversizePacketException;

public class BCodeTest {

	@Test
	public void rw() throws OversizePacketException {
		BEncodeEncoder encoder = BEncodeEncoder.newInstance();
		encoder.write(List.of("a", "b"));
		encoder.write(Map.of("1", "2"));
		encoder.write("xxxx".getBytes());
		String content = encoder.toString();
//		String content = encoder
//			.newList().put("1").put("2").flush()
//			.newMap().put("aa", "bb").put("cc", "dd").flush()
//			.write("xxxx".getBytes())
//			.toString();
		System.out.println(content);
		BEncodeDecoder decoder = BEncodeDecoder.newInstance(content.getBytes());
		decoder.nextList().forEach(value -> System.out.println(new String((byte[]) value)));
		decoder.nextMap().forEach((key, value) -> {
			if(value instanceof Long) {
				System.out.println(key + "=" + value);
			} else {
				System.out.println(key + "=" + new String((byte[]) value));
			}
		});
		System.out.println(decoder.oddString());
		
	}
	
	@Test
	public void nullRW() throws OversizePacketException {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("a", 1);
		map.put("b", null);
		map.put("c", "c");
		String content = new String(BEncodeEncoder.encodeMap(map));
		System.out.println(content);
		var decoder = BEncodeDecoder.newInstance(content);
		decoder.nextMap().forEach((key, value) -> {
			if(value instanceof Number) {
				System.out.println(key + "-" + value);
			} else {
				System.out.println(key + "-" + new String((byte[]) value));
			}
		});
	}
	
	@Test
	public void error() throws OversizePacketException {
		var decoder = BEncodeDecoder.newInstance("d8:completei6e10:downloadedi17e10:incompletei0e8:intervali924e12:min intervali462e5:peers36:����m�Wj���LmA�s;I�ʆL��TTz�e");
		var map = decoder.nextMap();
		System.out.println(map);
	}
	
}
