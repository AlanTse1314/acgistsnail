package com.acgist.snail.system.format;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>JSON处理工具</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class JSON {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSON.class);
	
	/**
	 * <p>JSON特殊字符</p>
	 * <p>Chrome浏览器控制台执行以下代码获取特殊字符：</p>
	 * <pre>
	 * for (var i = 0, value = '', array = []; i < 0xFFFF; i++) {
	 * 		value = JSON.stringify(String.fromCharCode(i));
	 * 		value.indexOf("\\") > -1 && array.push(value);
	 * }
	 * console.log(array.join(", "));
	 * </pre>
	 * <p>其他特殊字符（不处理）：D800~DFFF</p>
	 */
	private static final char[] CHARS = new char[] {
		'\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
		'\u0006', '\u0007', '\b', '\t', '\n', '\u000b', '\f', '\r',
		'\u000e', '\u000f', '\u0010', '\u0011', '\u0012', '\u0013',
		'\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019',
		'\u001a', '\u001b', '\u001c', '\u001d', '\u001e', '\u001f',
		'\"', '\\'
	};
	/**
	 * <p>特殊字符对应编码</p>
	 */
	private static final String[] CHARS_ENCODE = new String[] {
		"\\u0000", "\\u0001", "\\u0002", "\\u0003", "\\u0004", "\\u0005",
		"\\u0006", "\\u0007", "\\b", "\\t", "\\n", "\\u000b", "\\f", "\\r",
		"\\u000e", "\\u000f", "\\u0010", "\\u0011", "\\u0012", "\\u0013",
		"\\u0014", "\\u0015", "\\u0016", "\\u0017", "\\u0018", "\\u0019",
		"\\u001a", "\\u001b", "\\u001c", "\\u001d", "\\u001e", "\\u001f",
		"\\\"", "\\\\"
	};
	/**
	 * <p>{@code Map}前缀：{@value}</p>
	 */
	private static final char JSON_MAP_PREFIX = '{';
	/**
	 * <p>{@code Map}后缀：{@value}</p>
	 */
	private static final char JSON_MAP_SUFFIX = '}';
	/**
	 * <p>{@code List}前缀：{@value}</p>
	 */
	private static final char JSON_LIST_PREFIX = '[';
	/**
	 * <p>{@code List}后缀：{@value}</p>
	 */
	private static final char JSON_LIST_SUFFIX = ']';
	/**
	 * <p>键值分隔符：{@value}</p>
	 */
	private static final char JSON_KV_SEPARATOR = ':';
	/**
	 * <p>属性分隔符：{@value}</p>
	 */
	private static final char JSON_ATTR_SEPARATOR = ',';
	/**
	 * <p>字符串：{@value}</p>
	 */
	private static final char JSON_STRING = '"';
	/**
	 * <p>{@code null}：{@value}</p>
	 */
	private static final String JSON_NULL = "null";
	/**
	 * <p>{@code boolean}类型：{@value}</p>
	 */
	private static final String JSON_BOOLEAN_TRUE = "true";
	/**
	 * <p>{@code boolean}类型：{@value}</p>
	 */
	private static final String JSON_BOOLEAN_FALSE = "false";
	
	/**
	 * <p>JSON数据类型</p>
	 */
	public enum Type {

		/** map */
		MAP,
		/** list */
		LIST;
		
	}
	
	/**
	 * <p>类型</p>
	 */
	private Type type;
	/**
	 * <p>List</p>
	 */
	private List<Object> list;
	/**
	 * <p>Map</p>
	 */
	private Map<Object, Object> map;
	
	private JSON() {
	}
	
	/**
	 * <p>使用{@code map}生成JSON对象</p>
	 * 
	 * @param map {@code Map}
	 * 
	 * @return JSON对象
	 */
	public static final JSON ofMap(Map<Object, Object> map) {
		final JSON json = new JSON();
		json.map = map;
		json.type = Type.MAP;
		return json;
	}
	
	/**
	 * <p>使用{@code list}生成JSON对象</p>
	 * 
	 * @param list {@code List}
	 * 
	 * @return JSON对象
	 */
	public static final JSON ofList(List<Object> list) {
		final JSON json = new JSON();
		json.list = list;
		json.type = Type.LIST;
		return json;
	}

	/**
	 * <p>将字符串转为为JSON对象</p>
	 * 
	 * @param content 字符串
	 * 
	 * @return JSON对象
	 */
	public static final JSON ofString(String content) {
		if(StringUtils.isEmpty(content)) {
			throw new ArgumentException("JSON格式错误：" + content);
		}
		content = content.trim();
		final JSON json = new JSON();
		final char prefix = content.charAt(0);
		final char suffix = content.charAt(content.length() - 1);
		if(prefix == JSON_MAP_PREFIX && suffix == JSON_MAP_SUFFIX) {
			json.type = Type.MAP;
		} else if(prefix == JSON_LIST_PREFIX && suffix == JSON_LIST_SUFFIX) {
			json.type = Type.LIST;
		} else {
			throw new ArgumentException("JSON格式错误（类型）：" + content);
		}
		content = content.substring(1, content.length() - 1); // 去掉首位字符
		json.deserialize(content);
		return json;
	}
	
	/**
	 * <p>序列化JSON对象</p>
	 * 
	 * @return JSON字符串
	 */
	private String serialize() {
		final StringBuilder builder = new StringBuilder();
		if(this.type == Type.MAP) {
			this.serializeMap(this.map, builder);
		} else if(this.type == Type.LIST) {
			this.serializeList(this.list, builder);
		} else {
			throw new ArgumentException("JSON类型错误：" + this.type);
		}
		return builder.toString();
	}

	/**
	 * <p>序列化Map</p>
	 * 
	 * @param map Map
	 * @param builder JSON字符串Builder
	 */
	private void serializeMap(Map<?, ?> map, StringBuilder builder) {
		Objects.requireNonNull(map, "JSON序列化错误（Map为空）");
		builder.append(JSON_MAP_PREFIX);
		if(!map.isEmpty()) {
			map.entrySet().forEach(entry -> {
				serializeValue(entry.getKey(), builder);
				builder.append(JSON_KV_SEPARATOR);
				serializeValue(entry.getValue(), builder);
				builder.append(JSON_ATTR_SEPARATOR);
			});
			builder.setLength(builder.length() - 1);
		}
		builder.append(JSON_MAP_SUFFIX);
	}
	
	/**
	 * <p>序列化List</p>
	 * 
	 * @param list List
	 * @param builder JSON字符串Builder
	 */
	private void serializeList(List<?> list, StringBuilder builder) {
		Objects.requireNonNull(list, "JSON序列化错误（List为空）");
		builder.append(JSON_LIST_PREFIX);
		if(!list.isEmpty()) {
			list.forEach(value -> {
				serializeValue(value, builder);
				builder.append(JSON_ATTR_SEPARATOR);
			});
			builder.setLength(builder.length() - 1);
		}
		builder.append(JSON_LIST_SUFFIX);
	}
	
	/**
	 * <p>序列化Java对象</p>
	 * 
	 * @param object Java对象
	 * @param builder JSON字符串
	 */
	private void serializeValue(Object object, StringBuilder builder) {
		if(object == null) {
			builder.append(JSON_NULL);
		} else if(object instanceof String) {
			builder.append(JSON_STRING).append(encodeValue((String) object)).append(JSON_STRING);
		} else if(object instanceof Boolean) {
			builder.append(object.toString());
		} else if(object instanceof Number) {
			builder.append(object.toString());
		} else if(object instanceof JSON) {
			builder.append(object.toString());
		} else if(object instanceof Map) {
			serializeMap((Map<?, ?>) object, builder);
		} else if(object instanceof List) {
			serializeList((List<?>) object, builder);
		} else {
			builder.append(JSON_STRING).append(encodeValue(object.toString())).append(JSON_STRING);
		}
	}
	
	/**
	 * <p>转义JSON字符串</p>
	 * 
	 * @param content 待转义字符串
	 * 
	 * @return 转义后字符串
	 */
	private String encodeValue(String content) {
		int index = -1;
		final char[] chars = content.toCharArray();
		final StringBuilder builder = new StringBuilder();
		for (char value : chars) {
			index = ArrayUtils.indexOf(CHARS, value);
			if(index == ArrayUtils.NO_INDEX) {
				builder.append(value);
			} else {
				builder.append(CHARS_ENCODE[index]);
			}
		}
		return builder.toString();
	}
	
	/**
	 * <p>反序列化JSON字符串</p>
	 * 
	 * @param content JSON字符串
	 */
	private void deserialize(String content) {
		if(this.type == Type.MAP) {
			deserializeMap(content);
		} else if(this.type == Type.LIST) {
			deserializeList(content);
		} else {
			throw new ArgumentException("JSON类型错误：" + this.type);
		}
	}
	
	/**
	 * <p>反序列化Map</p>
	 * 
	 * @param content JSON字符串
	 */
	private void deserializeMap(String content) {
		this.map = new LinkedHashMap<>();
		final AtomicInteger index = new AtomicInteger(0);
		while(index.get() < content.length()) {
			this.map.put(
				deserializeValue(index, content),
				deserializeValue(index, content)
			);
		}
	}
	
	/**
	 * <p>反序列化List</p>
	 * 
	 * @param content JSON字符串
	 */
	private void deserializeList(String content) {
		this.list = new ArrayList<>();
		final AtomicInteger index = new AtomicInteger(0);
		while(index.get() < content.length()) {
			this.list.add(
				deserializeValue(index, content)
			);
		}
	}
	
	/**
	 * <p>反序列化JSON字符串</p>
	 * 
	 * @param index 字符索引
	 * @param content JSON字符串
	 * 
	 * @return Java对象
	 */
	private Object deserializeValue(AtomicInteger index, String content) {
		char value;
		boolean string = false; // 是否是字符串对象
		boolean json = false; // 是否是JSON对象
		String hexValue;
		final StringBuilder builder = new StringBuilder();
		do {
			value = content.charAt(index.get());
			if(value == JSON_STRING) {
				if(string) {
					string = false;
				} else {
					string = true;
				}
			} else if(value == JSON_MAP_PREFIX || value == JSON_LIST_PREFIX) {
				json = true;
			} else if(value == JSON_MAP_SUFFIX || value == JSON_LIST_SUFFIX) {
				json = false;
			}
			// 不属于JSON对象和字符串对象出现分隔符：结束循环
			if(!string && !json && (value == JSON_KV_SEPARATOR || value == JSON_ATTR_SEPARATOR)) {
				index.incrementAndGet();
				break;
			}
			if (value == '\\') { // 转义：参考{@link #BYTES}
				index.incrementAndGet();
				value = content.charAt(index.get());
				switch (value) {
				case 'b':
					builder.append('\b');
					break;
				case 't':
					builder.append('\t');
					break;
				case 'n':
					builder.append('\n');
					break;
				case 'f':
					builder.append('\f');
					break;
				case 'r':
					builder.append('\r');
					break;
				case '"':
					builder.append(value);
					break;
				case '\\':
					builder.append(value);
					break;
				case 'u': // Unicode
					hexValue = content.substring(index.get() + 1, index.get() + 5);
					builder.append((char) Integer.parseInt(hexValue, 16));
					index.addAndGet(4);
					break;
				default:
					builder.append(value);
					LOGGER.warn("不支持的JSON转义符号：{}", value);
					break;
				}
			} else {
				builder.append(value);
			}
		} while (index.incrementAndGet() < content.length());
		return convertValue(builder.toString());
	}
	
	/**
	 * <p>类型转换</p>
	 * 
	 * @param content JSON字符串
	 * 
	 * @return Java对象
	 */
	private Object convertValue(String content) {
		final String value = content.trim();
		final int length = value.length();
		if(
			length > 1 &&
			value.charAt(0) == JSON_STRING &&
			value.charAt(value.length() - 1) == JSON_STRING
		) { // 字符串
			return value.substring(1, length - 1); // 去掉引号
		} else if(
			JSON_BOOLEAN_TRUE.equals(value) ||
			JSON_BOOLEAN_FALSE.equals(value)
		) { // Boolean
			return Boolean.valueOf(value);
		} else if(JSON_NULL.equals(value)) { // null
			return null;
		} else if(StringUtils.isDecimal(value)) { // 数字
			return Integer.valueOf(value);
		} else if(
			length > 1 &&
			value.charAt(0) == JSON_MAP_PREFIX && value.charAt(length - 1) == JSON_MAP_SUFFIX
		) { // MAP：懒加载
//			return JSON.ofString(value);
			return value;
		} else if(
			length > 1 &&
			value.charAt(0) == JSON_LIST_PREFIX && value.charAt(length - 1) == JSON_LIST_SUFFIX
		) { // LIST：懒加载
//			return JSON.ofString(value);
			return value;
		} else {
			throw new ArgumentException("JSON格式错误：" + value);
		}
	}
	
	public Map<Object, Object> getMap() {
		return this.map;
	}
	
	public List<Object> getList() {
		return this.list;
	}
	
	/**
	 * <p>获取JSON对象</p>
	 * <p>如果对象是JSON对象直接返回，如果是字符串转为JSON对象。</p>
	 * 
	 * @param key 属性名称
	 * 
	 * @return JSON对象
	 */
	public JSON getJSON(Object key) {
		final Object value = get(key);
		if(value == null) {
			return null;
		} else if(value instanceof JSON) {
			return (JSON) value;
		} else if(value instanceof String) {
			return JSON.ofString((String) value);
		} else {
			throw new ArgumentException("JSON转换错误：" + value);
		}
	}
	
	/**
	 * <p>获取Integer属性</p>
	 * 
	 * @param key 属性名称
	 * 
	 * @return Integer
	 */
	public Integer getInteger(Object key) {
		return (Integer) this.get(key);
	}

	/**
	 * <p>获取Boolean属性</p>
	 * 
	 * @param key 属性名称
	 * 
	 * @return Boolean
	 */
	public Boolean getBoolean(Object key) {
		return (Boolean) this.get(key);
	}
	
	/**
	 * <p>获取String属性</p>
	 * 
	 * @param key 属性名称
	 * 
	 * @return String
	 */
	public String getString(Object key) {
		return (String) this.get(key);
	}
	
	/**
	 * <p>获取属性</p>
	 * 
	 * @param key 属性名称
	 * 
	 * @return 属性对象
	 */
	public Object get(Object key) {
		return this.map.get(key);
	}
	
	/**
	 * @return JSON字符串
	 */
	public String toJSON() {
		return this.serialize();
	}
	
	@Override
	public String toString() {
		return this.serialize();
	}
	
}
