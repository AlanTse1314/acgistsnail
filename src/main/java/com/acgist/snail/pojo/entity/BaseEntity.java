package com.acgist.snail.pojo.entity;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Entity - 数据库实体基类<br>
 * @Transient：不需要映射数据库的字段
 */
public class BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_ID = "id";
	public static final String PROPERTY_CLASS = "class";
	public static final String PROPERTY_CREATE_DATE = "createDate";
	public static final String PROPERTY_MODIFY_DATE = "modifyDate";
	
	public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * ID
	 */
	private String id;
	/**
	 * 创建日期
	 */
	private Date createDate;
	/**
	 * 修改日期
	 */
	private Date modifyDate;
	
	/**
	 * 获取ID，生成的uuid自动去掉“-”
	 * @return ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * 设置ID
	 * @param id ID
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 获取创建日期
	 * @return 创建日期
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * 设置创建日期
	 * @param createDate 创建日期
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	/**
	 * 获取修改日期
	 * @return 修改日期
	 */
	public Date getModifyDate() {
		return modifyDate;
	}

	/**
	 * 设置修改日期
	 * @param modifyDate 日期
	 */
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	/**
	 * 重写equals方法
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (!BaseEntity.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		BaseEntity other = (BaseEntity) obj;
		return new EqualsBuilder()
			.append(getId(), other.getId())
			.isEquals();
	}

	/**
	 * 重写hashCode方法
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(getId())
			.toHashCode();
	}
	
}
