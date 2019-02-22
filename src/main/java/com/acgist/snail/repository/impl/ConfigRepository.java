package com.acgist.snail.repository.impl;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.repository.BaseRepository;

/**
 * 配置
 */
public class ConfigRepository extends BaseRepository<ConfigEntity> {

	public ConfigRepository() {
		super(ConfigEntity.TABLE_NAME);
	}

	/**
	 * 更新配置：如果不存在保存，如果存在更新
	 */
	public void updateConfig(String name, String value) {
		ConfigEntity entity = findOne(ConfigEntity.PROPERTY_NAME, name);
		if(entity == null) {
			entity = new ConfigEntity();
			entity.setName(name);
			entity.setValue(value);
			save(entity);
		} else {
			entity.setValue(value);
			update(entity);
		}
	}

}
