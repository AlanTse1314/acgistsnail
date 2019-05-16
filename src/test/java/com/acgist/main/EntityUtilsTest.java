package com.acgist.main;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.utils.EntityUtils;

public class EntityUtilsTest {

	@Test
	public void property() {
		TaskEntity entity = new TaskEntity();
		entity.setName("测试");
		final String[] properties = EntityUtils.entityProperty(entity.getClass());
		final String sqlProperty = Stream.of(properties)
			.map(property -> "`" + property + "`")
			.collect(Collectors.joining(",", "(", ")"));
		final String sqlValue = Stream.of(properties)
			.map(property -> "?")
			.collect(Collectors.joining(",", "(", ")"));
		final Object[] parameters = Stream.of(properties)
			.map(property -> EntityUtils.entityPropertyValue(entity, property))
			.toArray();
		
		System.out.println(sqlProperty);
		System.out.println(sqlValue);
		System.out.println(Arrays.asList(parameters));
	}
	
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void unProperty() throws IntrospectionException {
		PropertyDescriptor descriptor = new PropertyDescriptor("status", TaskEntity.class);
		if(descriptor.getPropertyType().isEnum()) {
			System.out.println(Enum.valueOf(((Class<Enum>) descriptor.getPropertyType()), "pause"));
		}
	}
	
}
