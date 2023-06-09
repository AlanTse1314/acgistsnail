package com.acgist.snail.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.config.SymbolConfig;

/**
 * <p>Map工具</p>
 * 
 * @author acgist
 */
public class MapUtils {

	private MapUtils() {
	}
	
	/**
	 * <p>判断是否为空</p>
	 * 
	 * @param map Map
	 * 
	 * @return 是否为空
	 */
	public static final boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * <p>判断是否非空</p>
	 * 
	 * @param map Map
	 * 
	 * @return 是否非空
	 */
	public static final boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}
	
	/**
	 * <p>Map转为URL参数</p>
	 * 
	 * @param map Map
	 * 
	 * @return URL参数
	 */
	public static final String toUrlQuery(Map<String, String> map) {
		if(MapUtils.isEmpty(map)) {
			return null;
		}
		return map.entrySet().stream()
			.map(entry -> SymbolConfig.Symbol.EQUALS.join(entry.getKey(), UrlUtils.encode(entry.getValue())))
			.collect(Collectors.joining(SymbolConfig.Symbol.AND.toString()));
	}
	
	/**
	 * <p>获取对象</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 对象
	 */
	public static final Object get(Map<?, ?> map, String key) {
		if(map == null) {
			return null;
		}
		return map.get(key);
	}
	
	/**
	 * <p>获取字节</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 字节
	 */
	public static final Byte getByte(Map<?, ?> map, String key) {
		final Long value = getLong(map, key);
		if(value == null) {
			return null;
		}
		return value.byteValue();
	}
	
	/**
	 * <p>获取数值</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 数值
	 */
	public static final Integer getInteger(Map<?, ?> map, String key) {
		final Long value = getLong(map, key);
		if(value == null) {
			return null;
		}
		return value.intValue();
	}
	
	/**
	 * <p>获取数值</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 数值
	 */
	public static final Long getLong(Map<?, ?> map, String key) {
		if(map == null) {
			return null;
		}
		return (Long) map.get(key);
	}
	
	/**
	 * <p>获取字符串</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 字符串
	 */
	public static final String getString(Map<?, ?> map, String key) {
		return getString(map, key, null);
	}
	
	/**
	 * <p>获取字符串</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * @param encoding 编码
	 * 
	 * @return 字符串
	 */
	public static final String getString(Map<?, ?> map, String key, String encoding) {
		final var bytes = getBytes(map, key);
		if(bytes == null) {
			return null;
		}
		return StringUtils.getCharsetString(bytes, encoding);
	}
	
	/**
	 * <p>获取字节数组</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 字节数组
	 */
	public static final byte[] getBytes(Map<?, ?> map, String key) {
		if(map == null) {
			return null;
		}
		return (byte[]) map.get(key);
	}
	
	/**
	 * <p>获取集合</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 集合
	 */
	public static final List<Object> getList(Map<?, ?> map, String key) {
		if(map == null) {
			return List.of();
		}
		final var result = (List<?>) map.get(key);
		if(result == null) {
			return List.of();
		}
		return result.stream()
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>获取Map</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return Map
	 */
	public static final Map<String, Object> getMap(Map<?, ?> map, String key) {
		if(map == null) {
			return Map.of();
		}
		final var result = (Map<?, ?>) map.get(key);
		if(result == null) {
			return Map.of();
		}
		// 使用LinkedHashMap防止乱序
		return result.entrySet().stream()
			.collect(Collectors.toMap(
				entry -> (String) entry.getKey(),
//				entry -> entry.getKey() == null ? null : entry.getKey().toString(),
				Map.Entry::getValue,
				(a, b) -> b,
				LinkedHashMap::new
			));
	}
	
}
