package com.acgist.snail.logger;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>日志适配器</p>
 * 
 * @author acgist
 */
public abstract class LoggerAdapter {

	/**
	 * <p>常规日志输出流</p>
	 */
	protected OutputStream output;
	/**
	 * <p>异常日志输出流</p>
	 */
	protected OutputStream errorOutput;
	/**
	 * <p>是否可用</p>
	 * <p>系统关闭后如果继续输出日志导致异常</p>
	 */
	private volatile boolean available = true;

	protected LoggerAdapter() {
	}

	/**
	 * @param output 常规日志输出流
	 * @param errorOutput 异常日志输出流
	 */
	protected LoggerAdapter(OutputStream output, OutputStream errorOutput) {
		this.output = output;
		this.errorOutput = errorOutput;
	}
	
	/**
	 * <p>输出日志</p>
	 * 
	 * @param message 日志
	 */
	public void output(String message) {
		try {
			if(this.available) {
				this.output.write(message.getBytes());
			}
		} catch (IOException e) {
			Logger.error(e);
		}
	}
	
	/**
	 * <p>输出错误日志</p>
	 * 
	 * @param message 日志
	 */
	public void errorOutput(String message) {
		try {
			if(this.available) {
				this.errorOutput.write(message.getBytes());
			}
		} catch (IOException e) {
			Logger.error(e);
		}
	}
	
	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		this.available = false;
		if(this.output != null) {
			try {
				this.output.flush();
				this.output.close();
			} catch (IOException e) {
				Logger.error(e);
			}
		}
		if(this.errorOutput != null) {
			try {
				this.errorOutput.flush();
				this.errorOutput.close();
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}
	
}
