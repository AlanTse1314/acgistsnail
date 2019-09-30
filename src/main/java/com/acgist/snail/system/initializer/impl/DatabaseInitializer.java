package com.acgist.snail.system.initializer.impl;

import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.repository.DatabaseManager;
import com.acgist.snail.system.config.DatabaseConfig;
import com.acgist.snail.system.initializer.Initializer;

/**
 * <p>初始化数据库</p>
 * <p>如果数据库表没有创建执行创建语句。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DatabaseInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);
	
	private DatabaseManager databaseManager = DatabaseManager.getInstance();
	
	private DatabaseInitializer() {
	}
	
	public static final DatabaseInitializer newInstance() {
		return new DatabaseInitializer();
	}
	
	@Override
	protected void init() {
		if(exist()) { // 已经创建
			return;
		} else { // 未创建表
			buildTable();
		}
	}
	
	/**
	 * 判断表是否存在
	 */
	private boolean exist() {
		return this.databaseManager.haveTable(ConfigEntity.TABLE_NAME);
	}

	/**
	 * 初始化数据库表
	 */
	private void buildTable() {
		LOGGER.info("初始化数据库表");
		final String sql = buildTableSQL();
		this.databaseManager.update(sql);
	}

	/**
	 * 读取初始化SQL
	 */
	private String buildTableSQL() {
		final StringBuilder sql = new StringBuilder();
		final String tableSql = DatabaseConfig.getTableSQL();
		try(final var reader = new InputStreamReader(DatabaseInitializer.class.getResourceAsStream(tableSql))) {
			int count = 0;
			char[] chars = new char[1024];
			while((count = reader.read(chars)) != -1) {
				sql.append(new String(chars, 0, count));
			}
		} catch (Exception e) {
			LOGGER.error("建表SQL读取异常：{}", tableSql, e);
		}
		return sql.toString();
	}
	
}
