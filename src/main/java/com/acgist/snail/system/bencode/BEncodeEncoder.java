package com.acgist.snail.system.bencode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.bencode.BEncodeDecoder.Type;

/**
 * <p>B编码</p>
 * <p>支持数据类型：Number、String、byte[]。</p>
 * <pre>
 * encoder
 * 	.newList().put("1").put("2").flush()
 * 	.newMap().put("a", "b").put("c", "d").flush()
 * 	.toString();
 * 
 * encoder
 * 	.write(List.of("a", "b"))
 * 	.write(Map.of("1", "2"))
 * 	.toString();
 * </pre>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class BEncodeEncoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(BEncodeEncoder.class);
	
	/**
	 * list
	 */
	private List<Object> list;
	/**
	 * map
	 */
	private Map<String, Object> map;
	/**
	 * 数据类型
	 */
	private BEncodeDecoder.Type type;
	/**
	 * 输出数据，不需要关闭。
	 */
	private ByteArrayOutputStream outputStream;
	
	private BEncodeEncoder() {
		this.outputStream = new ByteArrayOutputStream();
	}
	
	public static final BEncodeEncoder newInstance() {
		return new BEncodeEncoder();
	}

	/**
	 * 新建List
	 */
	public BEncodeEncoder newList() {
		this.type = Type.LIST;
		this.list = new ArrayList<>();
		return this;
	}
	
	/**
	 * 新建Map
	 */
	public BEncodeEncoder newMap() {
		this.type = Type.MAP;
		this.map = new LinkedHashMap<>();
		return this;
	}
	
	/**
	 * 向List中添加数据
	 */
	public BEncodeEncoder put(Object value) {
		if(this.type == Type.LIST) {
			this.list.add(value);
		}
		return this;
	}
	
	/**
	 * 向List中添加数据
	 */
	public BEncodeEncoder put(List<?> list) {
		if(this.type == Type.LIST) {
			this.list.addAll(list);
		}
		return this;
	}
	
	/**
	 * 向Map中添加数据
	 */
	public BEncodeEncoder put(String key, Object value) {
		if(this.type == Type.MAP) {
			this.map.put(key, value);
		}
		return this;
	}
	
	/**
	 * 向Map中添加数据
	 */
	public BEncodeEncoder put(Map<String, Object> map) {
		if(this.type == Type.MAP) {
			this.map.putAll(map);
		}
		return this;
	}

	/**
	 * 将List和Map中的数据写入字符流，配合put系列方法使用。
	 */
	public BEncodeEncoder flush() {
		if(this.type == Type.MAP) {
			this.write(this.map);
		} else if(this.type == Type.LIST) {
			this.write(this.list);
		} else {
			LOGGER.warn("B编码不支持的类型：{}", this.type);
		}
		return this;
	}

	/**
	 * 写入List
	 */
	public BEncodeEncoder write(List<?> list) {
		if(list == null) {
			return this;
		}
		this.write(BEncodeDecoder.TYPE_L);
		list.forEach(value -> {
			this.writeBEncodeValue(value);
		});
		this.write(BEncodeDecoder.TYPE_E);
		return this;
	}
	
	/**
	 * 写入map
	 */
	public BEncodeEncoder write(Map<?, ?> map) {
		if(map == null) {
			return this;
		}
		this.write(BEncodeDecoder.TYPE_D);
		map.forEach((key, value) -> {
			final String keyValue = key.toString();
			final byte[] keyValues = keyValue.getBytes();
			this.writeBEncodeBytes(keyValues);
			this.writeBEncodeValue(value);
		});
		this.write(BEncodeDecoder.TYPE_E);
		return this;
	}
	
	/**
	 * 写入字符数组
	 */
	public BEncodeEncoder write(byte[] bytes) {
		try {
			if(bytes != null) {
				this.outputStream.write(bytes);
			}
		} catch (IOException e) {
			LOGGER.error("B编码输出异常", e);
		}
		return this;
	}
	
	/**
	 * 写入B编码对象
	 */
	private void writeBEncodeValue(Object value) {
		if(value instanceof Number) {
			this.writeBEncodeNumber((Number) value);
		} else if(value instanceof byte[]) {
			this.writeBEncodeBytes((byte[]) value);
		} else if(value instanceof String) {
			this.writeBEncodeBytes(((String) value).getBytes());
		} else if(value instanceof List) {
			write((List<?>) value);
		} else if(value instanceof Map) {
			write((Map<?, ?>) value);
		} else {
			this.writeBEncodeBytes(new byte[] {});
			LOGGER.debug("B编码不支持的类型：{}", value);
		}
	}
	
	/**
	 * 写入B编码数值
	 */
	private void writeBEncodeNumber(Number number) {
		this.write(BEncodeDecoder.TYPE_I);
		this.write(number.toString().getBytes());
		this.write(BEncodeDecoder.TYPE_E);
	}
	
	/**
	 * 写入B编码字符数组
	 */
	private void writeBEncodeBytes(byte[] bytes) {
		this.write(String.valueOf(bytes.length).getBytes());
		this.write(BEncodeDecoder.SEPARATOR);
		this.write(bytes);
	}
	
	/**
	 * 写入字符
	 */
	private void write(char value) {
		this.outputStream.write(value);
	}
	
	/**
	 * 获取字符流
	 */
	public byte[] bytes() {
		return this.outputStream.toByteArray();
	}

	/**
	 * 获取字符串，将关闭字符流。
	 */
	@Override
	public String toString() {
		return new String(bytes());
	}
	
	/**
	 * List转为B编码字符数组
	 */
	public static final byte[] encodeList(List<?> list) {
		return newInstance().write(list).bytes();
	}
	
	/**
	 * List转为B编码字符串
	 */
	public static final String encodeListString(List<?> list) {
		return new String(encodeList(list));
	}
	
	/**
	 * Map转为B编码字符数组
	 */
	public static final byte[] encodeMap(Map<?, ?> map) {
		return newInstance().write(map).bytes();
	}
	
	/**
	 * Map转为B编码字符串
	 */
	public static final String encodeMapString(Map<?, ?> map) {
		return new String(encodeMap(map));
	}
	
}
