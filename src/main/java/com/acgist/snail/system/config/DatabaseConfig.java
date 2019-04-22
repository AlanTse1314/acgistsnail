package com.acgist.snail.system.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库配置
 */
public class DatabaseConfig extends PropertiesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);
	
	private static final DatabaseConfig INSTANCE = new DatabaseConfig();
	
	private DatabaseConfig() {
		super("/config/config.database.properties");
	}

	static {
		LOGGER.info("初始化数据库配置");
		INSTANCE.init();
		INSTANCE.logger();
	}
	
	public static final DatabaseConfig getInstance() {
		return INSTANCE;
	}
	
	private String url;
	private String driver;
	private String user;
	private String password;
	private String tableSQL;

	/**
	 * 初始化
	 */
	private void init() {
		INSTANCE.url = getString("acgist.database.h2.url");
		INSTANCE.driver = getString("acgist.database.h2.driver");
		INSTANCE.user = getString("acgist.database.h2.user");
		INSTANCE.password = getString("acgist.database.h2.password");
		INSTANCE.tableSQL = getString("acgist.database.h2.table.sql");
	}
	
	/**
	 * 日志
	 */
	private void logger() {
		LOGGER.info("数据库地址：{}", INSTANCE.url);
		LOGGER.info("数据库驱动：{}", INSTANCE.driver);
		LOGGER.info("数据库用户：{}", INSTANCE.user);
		LOGGER.info("数据库密码：{}", INSTANCE.password);
		LOGGER.info("数据库建表语句：{}", INSTANCE.tableSQL);
	}
	
	/**
	 * 数据库地址
	 */
	public static final String getUrl() {
		return INSTANCE.url;
	}
	
	/**
	 * 数据库驱动
	 */
	public static final String getDriver() {
		return INSTANCE.driver;
	}

	/**
	 * 数据库用户账号
	 */
	public static final String getUser() {
		return INSTANCE.user;
	}

	/**
	 * 数据库用户密码
	 * @return
	 */
	public static final String getPassword() {
		return INSTANCE.password;
	}
	
	/**
	 * 数据库建表SQL文件
	 */
	public static final String getTableSQL() {
		return INSTANCE.tableSQL;
	}

}
