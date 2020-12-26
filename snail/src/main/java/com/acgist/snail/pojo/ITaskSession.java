package com.acgist.snail.pojo;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.downloader.IDownloader;

/**
 * <p>任务信息</p>
 * 
 * @author acgist
 */
public interface ITaskSession extends ITaskSessionTable, ITaskSessionEntity, ITaskSessionHandler, IStatisticsSessionGetter {

	/**
	 * <p>任务状态</p>
	 * 
	 * @author acgist
	 */
	public enum Status {
		
		/**
		 * <p>任务添加到下载队列时处于等待状态</p>
		 */
		AWAIT("等待中"),
		/**
		 * <p>任务下载时的状态：由下载管理器自动修改（不能直接设置此状态）</p>
		 */
		DOWNLOAD("下载中"),
		/**
		 * <p>任务暂停</p>
		 */
		PAUSE("暂停"),
		/**
		 * <p>任务完成：完成状态不能转换为其他任何状态</p>
		 */
		COMPLETE("完成"),
		/**
		 * <p>任务失败</p>
		 */
		FAIL("失败");
		
		/**
		 * <p>状态名称</p>
		 */
		private final String value;
		
		/**
		 * @param value 状态名称
		 */
		private Status(String value) {
			this.value = value;
		}

		/**
		 * <p>获取状态名称</p>
		 * 
		 * @return 状态名称
		 */
		public String getValue() {
			return value;
		}
		
	}
	
	/**
	 * <p>文件类型</p>
	 * 
	 * @author acgist
	 */
	public enum FileType {
		
		/** 图片 */
		IMAGE("图片"),
		/** 视频 */
		VIDEO("视频"),
		/** 音频 */
		AUDIO("音频"),
		/** 脚本 */
		SCRIPT("脚本"),
		/** BT */
		TORRENT("BT"),
		/** 压缩 */
		COMPRESS("压缩"),
		/** 文档 */
		DOCUMENT("文档"),
		/** 安装包 */
		INSTALL("安装包"),
		/** 未知 */
		UNKNOWN("未知");
		
		/**
		 * <p>类型名称</p>
		 */
		private final String value;

		/**
		 * @param value 类型名称
		 */
		private FileType(String value) {
			this.value = value;
		}

		/**
		 * <p>获取类型名称</p>
		 * 
		 * @return 类型名称
		 */
		public String getValue() {
			return value;
		}

	}
	
	/**
	 * <p>重置状态</p>
	 * <p>如果软件没有正常关闭，重置任务状态。</p>
	 */
	void reset();
	
	/**
	 * <p>获取下载器</p>
	 * 
	 * @return 下载器
	 */
	IDownloader downloader();

	/**
	 * <p>删除下载器</p>
	 * 
	 * @return 下载器
	 */
	IDownloader removeDownloader();
	
	/**
	 * <p>创建下载器</p>
	 * <p>如果已经存在下载器直接返回，否者创建下载器。</p>
	 * 
	 * @return 下载器
	 * 
	 * @throws DownloadException 下载异常
	 */
	IDownloader buildDownloader() throws DownloadException;
	
	/**
	 * <p>获取下载目录</p>
	 * 
	 * @return 下载目录
	 */
	File downloadFolder();
	
	/**
	 * <p>获取多文件下载任务选择下载文件列表</p>
	 * 
	 * @return 多文件下载任务选择下载文件列表
	 */
	List<String> multifileSelected();

	/**
	 * <p>获取已下载大小</p>
	 * 
	 * @return 已下载大小
	 */
	long downloadSize();
	
	/**
	 * <p>设置已下载大小</p>
	 * 
	 * @param size 已下载大小
	 */
	void downloadSize(long size);

	/**
	 * <p>判断任务是否处于等待状态</p>
	 * 
	 * @return 是否处于等待状态
	 */
	boolean await();
	
	/**
	 * <p>判断任务是否处于暂停状态</p>
	 * 
	 * @return 是否处于暂停状态
	 */
	boolean pause();
	
	/**
	 * <p>判断任务是否处于下载状态</p>
	 * 
	 * @return 是否处于下载状态
	 */
	boolean download();
	
	/**
	 * <p>判断任务是否处于完成状态</p>
	 * 
	 * @return 是否处于完成状态
	 */
	boolean complete();
	
	/**
	 * <p>判断任务是否处于执行状态</p>
	 * <p>执行状态（在线程池中）：等待中、下载中</p>
	 * 
	 * @return 是否处于执行状态
	 * 
	 * @see #await()
	 * @see #download()
	 */
	boolean inThreadPool();
	
	/**
	 * <p>获取任务信息（Map）</p>
	 * 
	 * @return 任务信息
	 */
	Map<String, Object> taskMessage();
	
}
