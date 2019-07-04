package com.acgist.snail.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.wrapper.ResultSetWrapper;

/**
 * <p>Bean工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class BeanUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtils.class);
	
	/**
	 * 调用默认构造方法（无参）生成实例
	 */
	public static final <T> T newInstance(final Class<T> clazz) {
		if(clazz == null) {
			return null;
		}
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			LOGGER.error("通过反射获取实例异常", e);
		}
		return null;
	}
	
	/**
	 * <p>获取类属性（非静态、非瞬时）</p>
	 */
	public static final String[] properties(Class<?> clazz) {
		String[] properties = null;
		final Class<?> superClazz = clazz.getSuperclass();
		if(superClazz != null) {
			properties = properties(superClazz);
		} else {
			properties = new String[0];
		}
		final Field[] fields = clazz.getDeclaredFields();
		return Stream.concat(
			Stream
				.of(fields)
				.filter(field -> {
					return !Modifier.isStatic(field.getModifiers()) && // 非静态属性
						!Modifier.isTransient(field.getModifiers()); // 非瞬时属性
				})
				.map(field -> field.getName()),
			Stream.of(properties)
		).toArray(String[]::new);
	}
	
	/**
	 * 获取属性值
	 */
	public static final Object[] propertiesValue(Object entity, String[] properties) {
		return Stream
			.of(properties)
			.map(property -> propertyValue(entity, property))
			.toArray();
	}
	
	/**
	 * <p>获取属性值</p>
	 * <p>属性必须有getter。</p>
	 */
	public static final Object propertyValue(Object entity, String property) {
		final Class<?> clazz = entity.getClass();
		try {
			final PropertyDescriptor descriptor = new PropertyDescriptor(property, clazz);
			return descriptor.getReadMethod().invoke(entity);
		} catch (Exception e) {
			LOGGER.error("反射属性获取异常", e);
		}
		return null;
	}
	
	/**
	 * <p>属性装配</p>
	 * <p>属性必须有setter。</p>
	 */
	public static final void entity(Object entity, ResultSetWrapper wrapper) {
		final Class<?> clazz = entity.getClass();
		final String[] properties = properties(clazz);
		for (String property : properties) {
			try {
				final PropertyDescriptor descriptor = new PropertyDescriptor(property, clazz);
				final Object value = unpack(descriptor.getPropertyType(), wrapper.getObject(property));
				descriptor.getWriteMethod().invoke(entity, value);
			} catch (Exception e) {
				LOGGER.info("反射属性装配异常", e);
			}
		}
	}
	
	/**
	 * <p>类型转换</p>
	 * <p>枚举类型转换为字符串类型。</p>
	 */
	public static final Object pack(Object object) {
		if(object == null) {
			return null;
		}
		if(object instanceof Enum<?>) { // 枚举类型
			final Enum<?> value = (Enum<?>) object;
			return value.name();
		}
		return object;
	}
	
	/**
	 * 类型转换：TODO：泛型优化
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Object unpack(Class<?> clazz, Object value) {
		if(value == null) {
			return null;
		}
		if(clazz.isEnum()) { // 枚举类型
			return Enum.valueOf((Class<Enum>) clazz, value.toString());
		}
		return value;
	}
	
}
