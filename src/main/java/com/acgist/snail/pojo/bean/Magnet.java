package com.acgist.snail.pojo.bean;

import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>磁力链接</p>
 * <p>只支持单个文件的Magnet下载，不支持多个文件。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class Magnet {

	/**
	 * 磁力链接类型
	 */
	public enum Type {
		
		/** md5 */
		MD5(		"urn:md5:"),
		/** aich */
		AICH(		"urn:aich:"),
		/** btih */
		BTIH(		"urn:btih:"),
		/** ed2k */
		ED2K(		"urn:ed2k:"),
		/** sha1 */
		SHA1(		"urn:sha1:"),
		/** crc32 */
		CRC32(		"urn:crc32:"),
		/** tth */
		TTH(		"urn:tree:tiger:"),
		/** bitprint */
		BITPRINT(	"urn:bitprint:");
		
		/**
		 * xt
		 */
		private final String xt;
		
		private Type(String xt) {
			this.xt = xt;
		}
		
		public String xt() {
			return this.xt;
		}
		
	}
	
	/**
	 * 类型
	 */
	private Type type;
	/**
	 * 显示名称
	 */
	private String dn;
	/**
	 * Tracker服务器列表
	 */
	private List<String> tr;
	/**
	 * 资源URN
	 */
	private String xt;
	/**
	 * 文件连接（普通链接）
	 */
	private String as;
	/**
	 * 绝对资源（P2P链接）
	 */
	private String xs;
	/**
	 * 绝对长度（字节）
	 */
	private String xl;
	/**
	 * 文件列表
	 */
	private String mt;
	/**
	 * 关键字（用于搜索）
	 */
	private String kt;
	/**
	 * xt中携带的文件hash
	 */
	private String hash;

	/**
	 * 添加Tracker服务器
	 */
	public void addTr(String tr) {
		if(this.tr == null) {
			this.tr = new ArrayList<>();
		}
		this.tr.add(tr);
	}
	
	/**
	 * 是否支持下载
	 */
	public boolean supportDownload() {
		return this.type == Type.BTIH && StringUtils.isNotEmpty(this.hash);
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public List<String> getTr() {
		return tr;
	}

	public void setTr(List<String> tr) {
		this.tr = tr;
	}

	public String getXt() {
		return xt;
	}

	public void setXt(String xt) {
		this.xt = xt;
	}

	public String getAs() {
		return as;
	}

	public void setAs(String as) {
		this.as = as;
	}

	public String getXs() {
		return xs;
	}

	public void setXs(String xs) {
		this.xs = xs;
	}

	public String getXl() {
		return xl;
	}

	public void setXl(String xl) {
		this.xl = xl;
	}

	public String getMt() {
		return mt;
	}

	public void setMt(String mt) {
		this.mt = mt;
	}

	public String getKt() {
		return kt;
	}

	public void setKt(String kt) {
		this.kt = kt;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.type, this.hash, this.dn, this.tr);
	}
	
}
