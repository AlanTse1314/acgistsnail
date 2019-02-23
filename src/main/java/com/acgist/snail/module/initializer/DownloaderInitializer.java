package com.acgist.snail.module.initializer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderBuilder;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.repository.impl.TaskRepository;

/**
 * 下载器初始化
 */
public class DownloaderInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderInitializer.class);
	
	public void init() {
		LOGGER.info("初始化下载器");
		TaskRepository repository = new TaskRepository();
		List<TaskEntity> list = repository.findAll();
		if(list != null && !list.isEmpty()) {
			list.forEach(entity -> {
				DownloaderBuilder builder = DownloaderBuilder.createBuilder(entity);
				builder.build();
			});
		}
	}
	
}
