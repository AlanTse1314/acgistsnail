package com.acgist.snail.window.build;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.coder.torrent.TorrentDecoder;
import com.acgist.snail.downloader.DownloaderBuilder;
import com.acgist.snail.module.config.DownloadConfig;
import com.acgist.snail.module.exception.DownloadException;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.window.AlertWindow;
import com.acgist.snail.window.main.TaskTimer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class BuildController implements Initializable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BuildController.class);
	
	@FXML
    private FlowPane root;
	@FXML
	private TextField urlValue;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 文件拖拽
		root.setOnDragOver(dragOverAction);
		root.setOnDragDropped(dragDroppedAction);
	}
	
	@FXML
	public void handleTorrentAction(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("选择种子文件");
		DownloadConfig.lastPath(chooser);
		chooser.getExtensionFilters().add(new ExtensionFilter("种子文件", "*.torrent"));
		File file = chooser.showOpenDialog(new Stage());
		if (file != null) {
			DownloadConfig.setDownloadLastPath(file.getParent());
			urlValue.setText(file.getPath());
		}
	}

	@FXML
	public void handleBuildAction(ActionEvent event) {
		String url = urlValue.getText();
		if(StringUtils.isEmpty(url)) {
			return;
		}
		boolean ok = true;
		try {
			DownloaderBuilder.newBuilder(url).build();
		} catch (DownloadException e) {
			ok = false;
			LOGGER.error("新建下载任务异常：{}", url, e);
			AlertWindow.warn("下载失败", e.getMessage());
		}
		if(ok) {
			urlValue.setText("");
			BuildWindow.getInstance().hide();
			TaskTimer.getInstance().refreshTaskTable();
		}
	}

	@FXML
	public void handleCancelAction(ActionEvent event) {
		BuildWindow.getInstance().hide();
	}
	
	public void setUrl(String url) {
		urlValue.setText(url);
	}
	
	private EventHandler<DragEvent> dragOverAction = (event) -> {
		if (event.getGestureSource() != root) {
			Dragboard dragboard = event.getDragboard();
			if(dragboard.hasFiles()) {
				File file = dragboard.getFiles().get(0);
				if(TorrentDecoder.verify(file.getPath())) {
					event.acceptTransferModes(TransferMode.COPY);
				} else {
					event.acceptTransferModes(TransferMode.NONE);
				}
			} else {
				event.acceptTransferModes(TransferMode.NONE);
			}
		}
		event.consume();
	};
	
	private EventHandler<DragEvent> dragDroppedAction = (event) -> {
		Dragboard dragboard = event.getDragboard();
		if (dragboard.hasFiles()) {
			File file = dragboard.getFiles().get(0);
			setUrl(file.getPath());
		}
		event.setDropCompleted(true);
		event.consume();
	};
	
}
