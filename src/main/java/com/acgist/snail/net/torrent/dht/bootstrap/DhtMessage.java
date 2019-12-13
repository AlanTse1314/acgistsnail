package com.acgist.snail.net.torrent.dht.bootstrap;

import java.net.InetSocketAddress;
import java.util.List;

import com.acgist.snail.system.config.DhtConfig;

/**
 * <p>DHT消息</p>
 * <p>DHT请求、DHT响应</p>
 *
 * @author acgist
 * @since 1.1.0
 */
public abstract class DhtMessage {

	/**
	 * <p>消息ID</p>
	 * <p>消息ID=请求ID=响应ID</p>
	 */
	protected final byte[] t;
	/**
	 * <p>消息类型</p>
	 */
	protected final String y;
	/**
	 * <p>地址：请求、响应</p>
	 */
	protected InetSocketAddress socketAddress;

	public DhtMessage(byte[] t, String y) {
		this.t = t;
		this.y = y;
	}

	public byte[] getT() {
		return t;
	}

	public String getY() {
		return y;
	}
	
	public InetSocketAddress getSocketAddress() {
		return socketAddress;
	}

	public void setSocketAddress(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}
	
	/**
	 * <p>获取消息ID</p>
	 * 
	 * @return 消息ID
	 */
	public byte[] getId() {
		return getT();
	}
	
	/**
	 * <p>获取NodeId</p>
	 * 
	 * @return NodeId
	 */
	public byte[] getNodeId() {
		return getBytes(DhtConfig.KEY_ID);
	}
	
	/**
	 * <p>获取Integer参数</p>
	 * 
	 * @param key 参数名称
	 * 
	 * @return Integer参数
	 */
	public Integer getInteger(String key) {
		final Long value = getLong(key);
		if(value == null) {
			return null;
		}
		return value.intValue();
	}
	
	/**
	 * <p>获取字符串参数</p>
	 * 
	 * @param key 参数名称
	 * 
	 * @return 字符串参数
	 */
	public String getString(String key) {
		final byte[] bytes = getBytes(key);
		if(bytes == null) {
			return null;
		}
		return new String(bytes);
	}
	
	/**
	 * <p>获取List参数</p>
	 * 
	 * @param key 参数名称
	 * 
	 * @return List参数
	 */
	public List<?> getList(String key) {
		return (List<?>) this.get(key);
	}
	
	/**
	 * <p>获取Long参数</p>
	 * 
	 * @param key 参数名称
	 * 
	 * @return Long参数
	 */
	public Long getLong(String key) {
		return (Long) this.get(key);
	}
	
	/**
	 * <p>获取byte[]数组参数</p>
	 * 
	 * @param key 参数名称
	 * 
	 * @return byte[]参数
	 */
	public byte[] getBytes(String key) {
		return (byte[]) this.get(key);
	}
	
	/**
	 * 
	 * <p>获取Object参数</p>
	 * 
	 * @param key 参数名称
	 * 
	 * @return Object参数
	 */
	public abstract Object get(String key);
	
	/**
	 * <p>设置参数</p>
	 * 
	 * @param key 参数名称
	 * @param value 参数值
	 */
	public abstract void put(String key, Object value);
	
}
