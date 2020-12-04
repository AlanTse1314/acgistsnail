package com.acgist.snail.logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.event.Level;

import com.acgist.snail.config.PropertiesConfig;
import com.acgist.snail.config.SystemConfig;

/**
 * <p>日志配置</p>
 * 
 * @author acgist
 */
public final class LoggerConfig {

	private static final LoggerConfig INSTANCE = new LoggerConfig();
	
	public static final LoggerConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>配置文件：{@value}</p>
	 */
	private static final String LOGGER_CONFIG = "/logger.properties";
	
	static {
		INSTANCE.init();
	}
	
	private LoggerConfig() {
		Properties properties = null;
		try(final var input = new InputStreamReader(PropertiesConfig.class.getResourceAsStream(LOGGER_CONFIG), SystemConfig.DEFAULT_CHARSET)) {
			properties = new Properties();
			properties.load(input);
		} catch (IOException e) {
			Logger.error(e);
		}
		this.properties = properties;
	}
	
	/**
	 * <p>配置信息</p>
	 */
	private final Properties properties;
	/**
	 * <p>日志名称</p>
	 */
	private String name;
	/**
	 * <p>日志级别</p>
	 */
	private String level;
	/**
	 * <p>日志适配</p>
	 */
	private String adapter;
	/**
	 * <p>文件日志名称</p>
	 */
	private String fileName;
	/**
	 * <p>文件日志缓存</p>
	 */
	private int fileBuffer;
	/**
	 * <p>文件日志最大备份数量</p>
	 */
	private int fileMaxSize;
	
	/**
	 * <p>初始化配置</p>
	 */
	private void init() {
		this.name = this.properties.getProperty("logger.name");
		this.level = this.properties.getProperty("logger.level");
		this.adapter = this.properties.getProperty("logger.adapter");
		this.fileName = this.properties.getProperty("logger.file.name");
		this.fileBuffer = Integer.parseInt(this.properties.getProperty("logger.file.buffer", "8192"));
		this.fileMaxSize = Integer.parseInt(this.properties.getProperty("logger.file.max.size", "30"));
	}

	/**
	 * <p>获取日志名称</p>
	 * 
	 * @return 日志名称
	 */
	public static final String getName() {
		return INSTANCE.name;
	}

	/**
	 * <p>获取日志级别</p>
	 * 
	 * @return 日志级别
	 */
	public static final String getLevel() {
		return INSTANCE.level;
	}

	/**
	 * <p>获取日志级别（数字）</p>
	 * 
	 * @return 日志级别（数字）
	 */
	public static final int getLevelInt() {
		int value = Level.DEBUG.toInt(); // 默认级别
		final Level[] levels = Level.values();
		for (Level level : levels) {
			if(level.name().equalsIgnoreCase(getLevel())) {
				value = level.toInt();
				break;
			}
		}
		return value;
	}
	
	/**
	 * <p>获取日志适配</p>
	 * 
	 * @return 日志适配
	 */
	public static final String getAdapter() {
		return INSTANCE.adapter;
	}

	/**
	 * <p>获取文件日志名称</p>
	 * 
	 * @return 文件日志名称
	 */
	public static final String getFileName() {
		return INSTANCE.fileName;
	}

	/**
	 * <p>获取文件日志缓存</p>
	 * 
	 * @return 文件日志缓存
	 */
	public static final int getFileBuffer() {
		return INSTANCE.fileBuffer;
	}

	/**
	 * <p>获取文件日志最大备份数量</p>
	 * 
	 * @return 文件日志最大备份数量
	 */
	public static final int getFileMaxSize() {
		return INSTANCE.fileMaxSize;
	}

}
