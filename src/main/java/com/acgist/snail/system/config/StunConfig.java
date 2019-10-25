package com.acgist.snail.system.config;

/**
 * STUN配置
 * 
 * @author acgist
 * @since 1.2.0
 */
public class StunConfig {

	/**
	 * 头信息长度
	 */
	public static final int STUN_HEADER_LENGTH = 20;
	/**
	 * 属性头信息长度
	 */
	public static final int ATTRIBUTE_HEADER_LENGTH = 4;
	/**
	 * 默认端口
	 */
	public static final int DEFAULT_PORT = 3478;
	/**
	 * 固定值
	 */
    public static final int MAGIC_COOKIE = 0x2112A442;
    /**
     * TransactionID长度
     */
    public static final int TRANSACTION_ID_LENGTH = 12;
    
    public static final int IPV4 = 0x01;
    public static final int IPV6 = 0x02;
    
    /**
     * 方法类型
     */
    public enum MethodType {
    	
    	/** 绑定：请求/响应、指示 */
    	BINDING((short) 0x01);
    	
    	public static final short VALUE_MASK = 0b0000_0000_0000_0001;
    	
    	MethodType(short value) {
    		this.value = value;
    	}
    	
    	/**
    	 * 方法ID
    	 */
    	private short value;
    	
    	public short vluae() {
    		return this.value;
    	}
    	
    }
    
    /**
     * 消息类型
     */
    public enum MessageType {
    	
    	/** 请求：服务器会响应 */
    	REQUEST(			(byte) 0b00),
    	/** 指示：服务器不响应 */
    	INDICATION(			(byte) 0b01),
    	/** 响应：成功 */
    	SUCCESS_RESPONSE(	(byte) 0b10),
    	/** 响应：失败 */
    	ERROR_RESPONSE(		(byte) 0b11);
    	
    	/**
    	 * C1
    	 */
        public static final short C1_MASK = 0b0000_0001_0000_0000;
        /**
         * C0
         */
        public static final short C0_MASK = 0b0000_0000_0001_0000;
        /**
         * 前两位必须为零
         */
        public static final short TYPE_MASK = 0b0011_1111_1111_1111;
        
        /**
         * 消息ID
         */
    	private byte value;
    	
    	MessageType(byte value) {
    		this.value = value;
    	}
    	
    	/**
    	 * 获取对应方法的MessageType值
    	 */
    	public short type(MethodType methodType) {
    		return (short) ((((this.value << 7) & C1_MASK) | ((this.value << 4) & C0_MASK) | methodType.value) & TYPE_MASK);
    	}
    	
    	/**
    	 * 消息类型转换
    	 */
    	public static final MessageType valueOf(short type) {
    		final byte value = (byte) ((((type & C1_MASK) >> 7) | ((type & C0_MASK) >> 4)) & 0xFF);
    		final var types = MessageType.values();
    		for (MessageType messageType : types) {
				if(messageType.value == value) {
					return messageType;
				}
			}
    		return null;
    	}
    	
    }
    
    /**
     * <p>属性类型</p>
     * <p>0x0000：保留</p>
     */
    public enum AttributeType {
    	
    	// 强制理解：0x0000-0x7FFF
    	/**  */
    	MAPPED_ADDRESS(		(short) 0x0001),
    	RESPONSE_ADDRESS(	(short) 0x0002),
    	CHANGE_ADDRESS(		(short) 0x0003),
    	SOURCE_ADDRESS(		(short) 0x0004),
    	CHANGED_ADDRESS(	(short) 0x0005),
    	USERNAME(			(short) 0x0006),
    	PASSWORD(			(short) 0x0007),
    	MESSAGE_INTEGRITY(	(short) 0x0008),
    	/** 错误：错误响应时使用 */
    	ERROR_CODE(			(short) 0x0009),
    	UNKNOWN_ATTRIBUTES(	(short) 0x000A),
    	REFLECTED_FROM(		(short) 0x000B),
    	REALM(				(short) 0x0014),
    	NONCE(				(short) 0x0015),
    	XOR_MAPPED_ADDRESS(	(short) 0x0020),
    	// 选择理解：0x8000-0xFFFF
    	SOFTWARE(			(short) 0x8022),
    	ALTERNATE_SERVER(	(short) 0x8023),
    	FINGERPRINT(		(short) 0x8028);

    	AttributeType(short value) {
    		this.value = value;
    	}
    	
    	/**
    	 * 属性ID
    	 */
    	private short value;
    	
    	public short value() {
    		return this.value;
    	}

		public static final AttributeType valueOf(short type) {
			final var types = AttributeType.values();
			for (AttributeType attributeType : types) {
				if(attributeType.value == type) {
					return attributeType;
				}
			}
			return null;
		}
    	
    }

    /**
     * 错误代码：300-699
     */
    public enum ErrorCode {
    	
    	/** 尝试替换 */
    	TRY_ALTERNATE(		300),
    	/** 请求错误 */
    	BAD_REQUEST(		400),
    	/** 没有授权 */
    	UNAUTHORIZED(		401),
    	/** 未知属性 */
    	UNKNOWN_ATTRIBUTE(	420),
    	/** NONCE过期 */
    	STALE_NONCE(		438),
    	/** 服务器错误 */
    	SERVER_ERROR(		500);
    	
    	ErrorCode(int code) {
    		this.code = code;
    	}
    	
    	private int code;
    	
    	public int code() {
    		return this.code;
    	}
    	
    }
	
}
