package com.acgist.snail.system.config;

/**
 * UTP配置
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpConfig {

	/**
	 * 帧类型
	 */
	public static final byte ST_DATA  = 0; // 数据
	public static final byte ST_FIN   = 1; // 结束
	public static final byte ST_STATE = 2; // 响应
	public static final byte ST_RESET = 3; // 重置
	public static final byte ST_SYN   = 4; // 握手
	
	/**
	 * 版本
	 */
	public static final byte UTP_VERSION = 1;

	/**
	 * 类型 + 版本
	 */
	public static final byte TYPE_DATA  = (ST_DATA 	<< 4) + UTP_VERSION;
	public static final byte TYPE_FIN   = (ST_FIN 	<< 4) + UTP_VERSION;
	public static final byte TYPE_STATE = (ST_STATE << 4) + UTP_VERSION;
	public static final byte TYPE_RESET = (ST_RESET << 4) + UTP_VERSION;
	public static final byte TYPE_SYN   = (ST_SYN 	<< 4) + UTP_VERSION;
	
	/**
	 * 扩展
	 */
	public static final byte EXTENSION = 0;
	
	/**
	 * UDP最大包长度
	 */
	public static final int MAX_PACKET_SIZE = 1472;
	
	/**
	 * 默认窗口大小
	 */
	public static final int WND_SIZE = 1024 * 1024;

	/**
	 * 最大发送次数
	 */
	public static final byte MAX_PUSH_TIMES = 3;
	
	/**
	 * 类型
	 */
	public static final String type(byte type) {
		return
			type == ST_DATA ? "DATA" :
			type == ST_FIN ? "FIN" :
			type == ST_STATE ? "STATE" :
			type == ST_RESET ? "RESET" :
			type == ST_SYN ? "SYN" : "UNKNOW";
	}
	
}
