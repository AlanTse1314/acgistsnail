package com.acgist.snail.utils;

/**
 * Object工具：toString、equals、hashCode等方法
 */
public class ObjectUtils {

	/**
	 * 重新hashCode方法
	 */
	public static final int hashCode(Object ... objects) {
		if(objects == null) {
			return 0;
		}
		final StringBuilder builder = new StringBuilder();
		for (Object object : objects) {
			if(object != null) {
				builder.append(object);
			}
		}
		return builder.toString().hashCode();
	}
	
	/**
	 * equals方法：判断是否相等
	 * @param source 源：this
	 * @param target 比较对象
	 */
	public static final boolean equals(Object source, Object target) {
		if(source == null) {
			return target == null;
		} else if(source == target) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * equals方法：判断是否可以相互访问
	 * @param source 源：this
	 * @param target 比较对象
	 */
	public static final boolean equalsClazz(Object source, Object target) {
		if(source.getClass().isAssignableFrom(target.getClass())) {
			return true;
		}
		return false;
	}
	
	/**
	 * equals对象id生成
	 */
	public static final String equalsBuilder(Object ... objects) {
		if(objects == null) {
			return null;
		}
		final StringBuilder builder = new StringBuilder();
		for (Object object : objects) {
			if(object != null) {
				builder.append(object);
			}
		}
		return builder.toString();
	}
	
	/**
	 * toString：返回JSON
	 */
	public static final String toString(Object object) {
		if(object == null) {
			return null;
		}
		return JsonUtils.toJson(object);
	}
	
}
