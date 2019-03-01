package com.acgist.snail.window.torrent;

import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.coder.torrent.TorrentDecoder;
import com.acgist.snail.coder.torrent.TorrentFiles;
import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.utils.JSONUtils;
import com.acgist.snail.window.AlertWindow;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TorrentController implements Initializable {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentController.class);
	
	private static final String HIDE_FILE_PREFIX = "_____padding_file"; // 不需要下载的文件前缀
	
	@FXML
    private FlowPane root;
	@FXML
	private Button download;
	@FXML
	private VBox treeBox;
	@FXML
	private VBox downloadBox;
	
	private TaskWrapper wrapper;
	private FileSelectManager manager;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 设置属性
		downloadBox.prefWidthProperty().bind(root.widthProperty());
		treeBox.prefWidthProperty().bind(root.widthProperty());
		downloadBox.prefHeightProperty().setValue(40D);;
		treeBox.prefHeightProperty().bind(root.heightProperty().subtract(40D));
		// 绑定事件
		download.setOnAction(downloadEvent);
	}

	/**
	 * 显示信息
	 */
	public void tree(TaskWrapper wrapper) {
		this.wrapper = wrapper;
		TreeView<HBox> tree = buildTree();
		TorrentDecoder decoder = TorrentDecoder.newInstance(wrapper.getTorrent());
		TorrentFiles files = decoder.torrentInfo().getInfo();
		manager = new FileSelectManager(files.getName(), download, tree);
		files.getFiles()
		.stream()
		.filter(file -> !file.path().startsWith(HIDE_FILE_PREFIX))
		.sorted((a, b) -> {
			return a.path().compareTo(b.path());
		})
		.forEach(file -> {
			manager.build(file.path(), file.getLength());
		});
	}
	
	/**
	 * 新建树形菜单
	 */
	private TreeView<HBox> buildTree() {
		TreeView<HBox> tree = new TreeView<>();
		tree.setId("tree");
		tree.getStyleClass().add("tree");
		tree.prefWidthProperty().bind(root.widthProperty());
		tree.prefHeightProperty().bind(treeBox.heightProperty());
		treeBox.getChildren().clear();
		treeBox.getChildren().add(tree);
		return tree;
	}
	
	/**
	 * 下载按钮事件
	 */
	private EventHandler<ActionEvent> downloadEvent = (event) -> {
		TaskEntity entity = wrapper.getEntity();
		var list = manager.description();
		if(list.isEmpty()) {
			AlertWindow.warn("下载提示", "请选择下载文件");
			return;
		}
		entity.setDescription(JSONUtils.javaToJson(list));
		TaskRepository repository = new TaskRepository();
		repository.update(entity);
		DownloaderManager.getInstance().refresh(wrapper);
		TorrentWindow.getInstance().hide();
	};
	
}
