package com.acgist.snail.gui.torrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.acgist.snail.gui.Tooltips;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.FileUtils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * <p>BT文件选择器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class SelectorManager {

	/**
	 * <p>下载按钮</p>
	 */
	private final Button download;
	/**
	 * <p>树形菜单根节点</p>
	 */
	private final TreeItem<HBox> root;
	/**
	 * <p>选择器MAP</p>
	 * <p>文件路径=选择文件</p>
	 */
	private final Map<String, Selector> selector = new HashMap<>();;

	/**
	 * <p>选择器</p>
	 * 
	 * @param name 任务名称
	 * @param download 下载按钮
	 * @param tree 树形菜单
	 */
	private SelectorManager(String name, Button download, TreeView<HBox> tree) {
		final TreeItem<HBox> root = buildTreeItem(null, "", name, null);
		root.setExpanded(true);
		tree.setRoot(root);
		this.root = root;
		this.download = download;
	}
	
	public static final SelectorManager newInstance(String name, Button download, TreeView<HBox> tree) {
		return new SelectorManager(name, download, tree);
	}

	/**
	 * <p>创建树形菜单</p>
	 * 
	 * @param path 文件路径
	 * @param size 文件大小
	 */
	public void build(String path, Long size) {
		String name = path;
		TreeItem<HBox> parent = this.root;
		// 包含路径时创建路径菜单
		if(path.contains(TorrentFile.SEPARATOR)) {
			String parentPath = "";
			TreeItem<HBox> treeItem = null;
			final String[] paths = path.split(TorrentFile.SEPARATOR);
			// 创建路径菜单
			for (int index = 0; index < paths.length - 1; index++) {
				final String value = paths[index];
				parentPath += value + TorrentFile.SEPARATOR;
				treeItem = buildTreeItem(parent, parentPath, value, null);
				parent = treeItem;
			}
			name = paths[paths.length - 1];
		}
		// 创建文件菜单
		buildTreeItem(parent, path, name, size);
	}
	
	/**
	 * <p>创建树形菜单</p>
	 * 
	 * @param parent 父节点
	 * @param path 路径
	 * @param name 名称
	 * @param size 大小
	 */
	private TreeItem<HBox> buildTreeItem(TreeItem<HBox> parent, String path, String name, Long size) {
		if(this.selector.containsKey(path)) { // 如果已经创建跳过：路径菜单
			return this.selector.get(path).getTreeItem();
		}
		final CheckBox checkBox = new CheckBox(name);
		checkBox.setPrefWidth(500);
		checkBox.setTooltip(Tooltips.newTooltip(name));
		checkBox.setOnAction(this.selectAction);
		final HBox box = new HBox(checkBox);
		// 设置文件大小
		if(size != null) {
			final Text text = new Text(FileUtils.formatSize(size));
			box.getChildren().add(text);
		}
		final TreeItem<HBox> treeItem = new TreeItem<HBox>(box);
		this.selector.put(path, new Selector(path, size, checkBox, treeItem));
		if(parent != null) { // 根节点没有父节点
			parent.getChildren().add(treeItem);
		}
		return treeItem;
	}
	
	/**
	 * @return 选择文件大小
	 */
	public Long size() {
		return this.selector.values().stream()
			.filter(value -> value.isSelected()) // 选中
			.filter(value -> value.isFile()) // 文件
			.collect(Collectors.summingLong(value -> value.getSize()));
	}
	
	/**
	 * @return 选择文件列表
	 */
	public List<String> description() {
		return this.selector.entrySet().stream()
			.filter(entry -> entry.getValue().isSelected()) // 选中
			.filter(entry -> entry.getValue().isFile()) // 文件
			.map(Entry::getKey)
			.collect(Collectors.toList());
	}

	/**
	 * <p>设置选择文件</p>
	 * <p>如果没有选中文件使用自动选择</p>
	 * <p>自动选择：选择大于平均值的文件</p>
	 */
	public void select(ITaskSession taskSession) {
		final var list = taskSession.selectTorrentFiles();
		if(CollectionUtils.isNotEmpty(list)) { // 已选择文件
			this.selector.entrySet().stream()
				.filter(entry -> list.contains(entry.getKey()))
				.forEach(entry -> entry.getValue().setSelected(true));
		} else { // 未选择文件：自动选择
			// 计算平均值
			final var avgSize = this.selector.values().stream()
				.collect(Collectors.averagingLong(Selector::getSize));
			this.selector.entrySet().stream()
				.filter(entry -> {
					return
						entry.getValue().isFile() && // 文件
						entry.getValue().getSize() >= avgSize; // 大于平均值
				})
				.forEach(entry -> entry.getValue().setSelected(true));
		}
		selectParentFolder();
		buttonSize();
	}
	
	/**
	 * <p>选择父目录</p>
	 * <p>选中文件时同时选中所有父目录</p>
	 */
	private void selectParentFolder() {
		final List<TreeItem<HBox>> parents = new ArrayList<>();
		// 所有父目录
		this.selector.values().stream()
			.filter(value -> value.isFile())
			.filter(value -> value.isSelected())
			.forEach(value -> {
				var parent = value.getTreeItem();
				while(parent.getParent() != null) {
					parent = parent.getParent();
					parents.add(parent);
				}
			});
		// 选择父目录
		this.selector.values().stream()
			.filter(value -> parents.contains(value.getTreeItem()))
			.forEach(value -> value.setSelected(true));
	}
	
	/**
	 * <p>设置按钮文本</p>
	 */
	private void buttonSize() {
		this.download.setText("下载（" + FileUtils.formatSize(size()) + "）");
	}
	
	/**
	 * <p>选择框事件</p>
	 * <p>选择子目录、选择父目录、计算选中文件大小</p>
	 */
	private EventHandler<ActionEvent> selectAction = (event) -> {
		final CheckBox checkBox = (CheckBox) event.getSource();
		final boolean selected = checkBox.isSelected();
		// 前缀
		final String prefix = this.selector.entrySet().stream()
			.filter(entry -> entry.getValue().getCheckBox() == checkBox)
			.map(entry -> entry.getKey())
			.findFirst().get();
		// 选择子目录
		this.selector.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(prefix))
			.forEach(entry -> entry.getValue().setSelected(selected));
		selectParentFolder();
		buttonSize();
	};
	
}

/**
 * <p>选择文件</p>
 */
class Selector {

	/**
	 * <p>文件路径</p>
	 */
	private final String path;
	/**
	 * <p>文件大小：文件夹=0</p>
	 */
	private final long size;
	/**
	 * <p>是否是文件：true=文件；false=文件夹；</p>
	 */
	private final boolean file;
	/**
	 * <p>选择框</p>
	 */
	private final CheckBox checkBox;
	/**
	 * <p>树形菜单节点</p>
	 */
	private final TreeItem<HBox> treeItem;

	public Selector(String path, Long size, CheckBox checkBox, TreeItem<HBox> treeItem) {
		this.path = path;
		this.size = (size == null || size == 0L) ? 0 : size;
		this.file = (size == null || size == 0L) ? false : true;
		this.checkBox = checkBox;
		this.treeItem = treeItem;
	}

	/**
	 * <p>是否选中</p>
	 * 
	 * @return true-选中；false-未选中；
	 */
	public boolean isSelected() {
		return this.checkBox.isSelected();
	}

	/**
	 * <p>设置选中</p>
	 * 
	 * @param selected true-选中；false-未选中；
	 */
	public void setSelected(boolean selected) {
		this.checkBox.setSelected(selected);
	}
	
	public String getPath() {
		return path;
	}

	public long getSize() {
		return size;
	}

	public boolean isFile() {
		return file;
	}

	public CheckBox getCheckBox() {
		return checkBox;
	}

	public TreeItem<HBox> getTreeItem() {
		return treeItem;
	}

}
