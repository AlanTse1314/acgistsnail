package com.acgist.snail.utils;

import java.util.Random;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.ArgumentException;

/**
 * <p>数组工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class ArrayUtils {

	/**
	 * 不存在索引
	 */
	public static final int NO_INDEX = -1;
	
	/**
	 * <p>数组比较</p>
	 * <p>相等：数组元素全部一致</p>
	 * 
	 * @param sources 原始数据
	 * @param targets 比较数据
	 */
	public static final boolean equals(byte[] sources, byte[] targets) {
		if(sources == targets) {
			return true;
		}
		if(sources == null || targets == null) {
			return false;
		}
		final int length = sources.length;
		if(length != targets.length) {
			return false;
		}
		for (int index = 0; index < length; index++) {
			if(sources[index] != targets[index]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * <p>比较数组大小（无符号比较）</p>
	 * <p>长度不同时：长度长的数组大</p>
	 * <p>长度相同时：高位字符（索引小）大的数组大</p>
	 * 
	 * @param sources 原始数据
	 * @param targets 比较数据
	 * 
	 * @return 1：原始数据大；-1：比较数据大；0：相等；
	 * 
	 * @since 1.1.0
	 */
	public static final int compareUnsigned(byte[] sources, byte[] targets) {
		if(sources == null || targets == null) {
			throw new ArgumentException("数组比较参数错误");
		} else if(sources.length != targets.length) {
			return sources.length > targets.length ? 1 : -1;
		} else {
			final int length = sources.length;
			for (int index = 0; index < length; index++) {
				if(sources[index] != targets[index]) {
					return ((char) sources[index]) > ((char) targets[index]) ? 1 : -1;
				}
			}
			return 0;
		}
	}
	
	/**
	 * 异或运算
	 * 
	 * @param sources 原始数据
	 * @param targets 比较数据
	 * 
	 * @return 结果
	 */
	public static final byte[] xor(byte[] sources, byte[] targets) {
		if (sources == null || targets == null) {
			throw new ArgumentException("异或运算参数错误");
		} else if (sources.length != targets.length) {
			throw new ArgumentException("异或运算参数错误（长度）");
		} else {
			final int length = sources.length;
			final byte[] result = new byte[length];
			for (int index = 0; index < length; index++) {
				result[index] = (byte) (sources[index] ^ targets[index]);
			}
			return result;
		}
	}
	
	/**
	 * <p>差异索引</p>
	 * <p>差异索引越小：差距越大</p>
	 * <p>差异索引越大：差距越小</p>
	 * 
	 * @param sources 原始数据
	 * @param targets 比较数据
	 * 
	 * @return 差异索引
	 */
	public static final int diffIndex(byte[] sources, byte[] targets) {
		if (sources == null || targets == null) {
			throw new ArgumentException("差异索引参数错误");
		} else if (sources.length != targets.length) {
			throw new ArgumentException("差异索引参数错误（长度）");
		} else {
			final int length = sources.length;
			for (int index = 0; index < length; index++) {
				if(sources[index] != targets[index]) {
					return index;
				}
			}
			return length;
		}
	}
	
	/**
	 * 空数组
	 */
	public static final boolean isEmpty(Object[] objects) {
		return objects == null || objects.length == 0;
	}
	
	/**
	 * 非空数组
	 */
	public static final boolean isNotEmpty(Object[] objects) {
		return !isEmpty(objects);
	}
	
	/**
	 * 空字节数组
	 */
	public static final boolean isEmpty(byte[] value) {
		return value == null || value.length == 0;
	}

	/**
	 * 非空字节数组
	 */
	public static final boolean isNotEmpty(byte[] value) {
		return !isEmpty(value);
	}
	
	/**
	 * 随机字符数组
	 */
	public static final byte[] random(int length) {
		final byte[] bytes = new byte[length];
		final Random random = NumberUtils.random();
		for (int index = 0; index < length; index++) {
			bytes[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_MAX);
		}
		return bytes;
	}
	
	/**
	 * 查找数组索引
	 * 
	 * @see {@link #indexOf(int[], int, int, int)}
	 */
	public static final int indexOf(int[] values, int value) {
		return indexOf(values, 0, values.length, value);
	}
	
	/**
	 * 查找数组索引
	 * 
	 * @param values 数组
	 * @param begin 开始位置
	 * @param end 结束位置
	 * @param value 查找数值
	 * 
	 * @return 索引：{@linkplain ArrayUtils#NO_INDEX -1}=不存在
	 */
	public static final int indexOf(int[] values, int begin, int end, int value) {
		for (int index = begin; index < end; index++) {
			if(values[index] == value) {
				return index;
			}
		}
		return ArrayUtils.NO_INDEX;
	}
	
	/**
	 * 查找字符索引
	 * 
	 * @param chars 数组
	 * @param value 查找字符
	 * 
	 * @return 索引：{@linkplain ArrayUtils#NO_INDEX -1}=不存在
	 */
	public static final int indexOf(char[] chars, char value) {
		for (int index = 0; index < chars.length; index++) {
			if(value == chars[index]) {
				return index;
			}
		}
		return ArrayUtils.NO_INDEX;
	}
	
}
