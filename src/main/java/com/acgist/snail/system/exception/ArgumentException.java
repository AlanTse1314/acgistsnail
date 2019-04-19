package com.acgist.snail.system.exception;

/**
 * 参数错误
 */
public class ArgumentException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ArgumentException() {
		super("参数错误");
	}

	public ArgumentException(String message) {
		super(message);
	}

	public ArgumentException(Throwable cause) {
		super(cause);
	}

	public ArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

}
