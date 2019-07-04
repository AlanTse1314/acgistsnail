package com.acgist.snail.pojo.session;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.gui.main.TaskDisplay;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.wrapper.TorrentFileSelectWrapper;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.system.context.SystemStatistics;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * Task Session
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TaskSession {

	private ThreadLocal<SimpleDateFormat> formater = new ThreadLocal<>() {
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm");
		};
	};
	
	private IDownloader downloader; // 下载器
	
	private final TaskEntity entity; // 任务
	private final StatisticsSession statistics; // 统计
	
	private TaskSession(TaskEntity entity) throws DownloadException {
		if(entity == null) {
			throw new DownloadException("创建下载任务失败");
		}
		this.entity = entity;
		this.statistics = new StatisticsSession(true, SystemStatistics.getInstance().getSystemStatistics());
	}
	
	// 功能 //
	public static final TaskSession newInstance(TaskEntity entity) throws DownloadException {
		return new TaskSession(entity);
	}
	
	public TaskEntity entity() {
		return this.entity;
	}
	
	public IDownloader downloader() {
		return this.downloader;
	}
	
	public void downloader(IDownloader downloader) {
		this.downloader = downloader;
	}
	
	/**
	 * 获取下载目录
	 */
	public File downloadFolder() {
		File file = new File(this.entity.getFile());
		if(file.isFile()) {
			return file.getParentFile();
		} else {
			return file;
		}
	}
	
	/**
	 * 获取已选择的下载文件
	 */
	public List<String> downloadTorrentFiles() {
		if(this.entity.getType() != Type.torrent) {
			return List.of();
		}
		String description = this.entity.getDescription();
		if(StringUtils.isEmpty(description)) {
			return List.of();
		} else {
			final TorrentFileSelectWrapper wrapper = TorrentFileSelectWrapper.newDecoder(description);
			return wrapper.list();
		}
	}

	/**
	 * 更新状态，刷新下载
	 */
	public void updateStatus(Status status) {
		if(complete()) {
			return;
		}
		TaskRepository repository = new TaskRepository();
		if(status == Status.complete) {
			this.entity.setEndDate(new Date()); // 设置完成时间
		}
		this.entity.setStatus(status);
		repository.update(this.entity);
		DownloaderManager.getInstance().refresh(); // 刷新下载
		TaskDisplay.getInstance().refreshTaskData(); // 刷新状态
	}
	
	public StatisticsSession statistics() {
		return this.statistics;
	}
	
	/**
	 * 已下载大小
	 */
	public long downloadSize() {
		return this.statistics.downloadSize();
	}
	
	/**
	 * 设置已下载大小
	 */
	public void downloadSize(long size) {
		this.statistics.downloadSize(size);
	}

	/**
	 * 等待状态
	 */
	public boolean await() {
		return this.entity.getStatus() == Status.await;
	}
	
	/**
	 * 下载状态
	 */
	public boolean download() {
		return this.entity.getStatus() == Status.download;
	}
	
	/**
	 * 等待状态
	 */
	public boolean pause() {
		return this.entity.getStatus() == Status.pause;
	}
	
	/**
	 * 完成状态
	 */
	public boolean complete() {
		return this.entity.getStatus() == Status.complete;
	}
	
	/**
	 * 任务执行状态：等待中或者下载中
	 */
	public boolean downloading() {
		return await() || download();
	}
	
	/**
	 * 获取下载任务
	 */
	public IDownloader buildDownloader() throws DownloadException {
		if(this.downloader != null) {
			return this.downloader;
		}
		return ProtocolManager.getInstance().buildDownloader(this);
	}
	
	// Table数据绑定 //
	
	/**
	 * 任务名称
	 */
	public String getNameValue() {
		return this.entity.getName();
	}

	/**
	 * 任务状态
	 */
	public String getStatusValue() {
		if(download()) {
			return FileUtils.formatSize(this.statistics.downloadSecond()) + "/S";
		} else {
			return this.entity.getStatus().getValue();
		}
	}
	
	/**
	 * 任务进度
	 */
	public String getProgressValue() {
		if(complete()) {
			return FileUtils.formatSize(this.entity.getSize());
		} else {
			return FileUtils.formatSize(this.statistics.downloadSize()) + "/" + FileUtils.formatSize(this.entity.getSize());
		}
	}

	/**
	 * 创建时间
	 */
	public String getCreateDateValue() {
		if(this.entity.getCreateDate() == null) {
			return "-";
		}
		return this.formater.get().format(this.entity.getCreateDate());
	}
	
	/**
	 * 完成时间
	 */
	public String getEndDateValue() {
		if(this.entity.getEndDate() == null) {
			if(download()) {
				final long downloadSecond = this.statistics.downloadSecond();
				if(downloadSecond == 0L) {
					return "-";
				} else {
					long second = (this.entity.getSize() - this.statistics.downloadSize()) / downloadSecond;
					return DateUtils.formatSecond(second);
				}
			} else {
				return "-";
			}
		}
		return this.formater.get().format(this.entity.getEndDate());
	}
	
}
