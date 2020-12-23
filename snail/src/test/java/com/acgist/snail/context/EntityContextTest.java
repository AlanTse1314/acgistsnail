package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.EntityException;
import com.acgist.snail.pojo.ITaskSession.FileType;
import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.Performance;

public class EntityContextTest extends Performance {
	
	@BeforeAll
	public static void load() {
		EntityContext.getInstance().load();
	}
	
	@Test
	public void testLoadCosted() {
		this.costed(10000, () -> EntityContext.getInstance().load());
		this.costed(10000, () -> EntityContext.getInstance().persistent());
	}

	@Test
	@Order(0)
	public void testSaveTask() {
		final TaskEntity entity = new TaskEntity();
		entity.setName("测试");
		entity.setType(Type.HTTP);
		entity.setFileType(FileType.VIDEO);
		EntityContext.getInstance().save(entity);
		assertNotNull(entity.getId());
		assertThrows(EntityException.class, () -> EntityContext.getInstance().save(entity));
	}

	@Test
	@Order(1)
	public void testUpdateTask() {
		final TaskEntity entity = new TaskEntity();
		assertThrows(EntityException.class, () -> EntityContext.getInstance().update(entity));
		entity.setName("测试");
		entity.setType(Type.HTTP);
		entity.setFileType(FileType.VIDEO);
		final Date modifyDate = new Date(System.currentTimeMillis() - 1000);
		entity.setModifyDate(modifyDate);
		EntityContext.getInstance().save(entity);
		EntityContext.getInstance().update(entity);
		assertNotEquals(modifyDate.getTime(), entity.getModifyDate().getTime());
	}
	
	@Test
	@Order(2)
	public void testDeleteTask() {
		final var list = new ArrayList<>(EntityContext.getInstance().allTask());
		list.forEach(entity -> {
			assertTrue(EntityContext.getInstance().delete(entity));
		});
	}
	
	@Test
	@Order(3)
	public void testSaveConfig() {
		final ConfigEntity entity = new ConfigEntity();
		entity.setName("acgist");
		entity.setValue("测试");
		EntityContext.getInstance().save(entity);
		assertNotNull(entity.getId());
		assertThrows(EntityException.class, () -> EntityContext.getInstance().save(entity));
	}

	@Test
	@Order(4)
	public void testUpdateConfig() {
		final ConfigEntity entity = new ConfigEntity();
		assertThrows(EntityException.class, () -> EntityContext.getInstance().update(entity));
		entity.setName("acgist");
		entity.setValue("测试");
		final Date modifyDate = new Date(System.currentTimeMillis() - 1000);
		entity.setModifyDate(modifyDate);
		EntityContext.getInstance().save(entity);
		EntityContext.getInstance().update(entity);
		assertNotEquals(modifyDate.getTime(), entity.getModifyDate().getTime());
	}
	
	@Test
	@Order(5)
	public void testDeleteConfig() {
		final var list = new ArrayList<>(EntityContext.getInstance().allConfig());
		list.forEach(entity -> {
			assertTrue(EntityContext.getInstance().delete(entity));
		});
	}

	@Test
	@Order(6)
	public void testFindConfig() {
		final ConfigEntity entity = new ConfigEntity();
		entity.setName("acgist");
		entity.setValue("测试");
		EntityContext.getInstance().save(entity);
		final var config = EntityContext.getInstance().findConfig("acgist");
		assertNotNull(config);
		this.log(config.getName() + "=" + config.getValue());
	}
	
	@Test
	@Order(7)
	public void testDeleteConfigName() {
		final ConfigEntity entity = new ConfigEntity();
		entity.setName("acgist");
		entity.setValue("测试");
		EntityContext.getInstance().save(entity);
		assertNotNull(EntityContext.getInstance().findConfig("acgist"));
		EntityContext.getInstance().deleteConfig("acgist");
		assertNull(EntityContext.getInstance().findConfig("acgist"));
	}
	
	@Test
	@Order(8)
	public void testMergeConfig() {
		EntityContext.getInstance().allConfig().forEach(this::log);
		EntityContext.getInstance().mergeConfig("acgist", "1234");
		EntityContext.getInstance().allConfig().forEach(this::log);
		EntityContext.getInstance().mergeConfig("acgist", "4321");
		EntityContext.getInstance().allConfig().forEach(this::log);
	}
	
	@Test
	@Order(9)
	public void testPersistent() {
		final TaskEntity taskEntity = new TaskEntity();
		EntityContext.getInstance().save(taskEntity);
		EntityContext.getInstance().persistent();
		this.costed(10000, () -> EntityContext.getInstance().persistent());
	}
	
}
