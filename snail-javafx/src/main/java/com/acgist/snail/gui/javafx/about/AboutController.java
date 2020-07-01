package com.acgist.snail.gui.javafx.about;

import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.gui.javafx.Controller;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.DesktopUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

/**
 * <p>关于窗口控制器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class AboutController extends Controller implements Initializable {
	
	@FXML
	private GridPane root;
	
	@FXML
	private Text name;
	@FXML
	private Text version;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.buildName();
		this.buildVersion();
	}

	/**
	 * <p>作者按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleAuthorAction(ActionEvent event) {
		DesktopUtils.browse(SystemConfig.getAuthor());
	}
	
	/**
	 * <p>官网与源码按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleSourceAction(ActionEvent event) {
		DesktopUtils.browse(SystemConfig.getSource());
	}
	
	/**
	 * <p>问题与建议按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleSupportAction(ActionEvent event) {
		DesktopUtils.browse(SystemConfig.getSupport());
	}

	/**
	 * <p>设置软件名称</p>
	 */
	private void buildName() {
		final StringBuilder builder = new StringBuilder();
		builder.append(SystemConfig.getName())
			.append("（")
			.append(SystemConfig.getNameEn())
			.append("）");
		this.name.setText(builder.toString());
	}
	
	/**
	 * <p>设置软件版本</p>
	 */
	private void buildVersion() {
		this.version.setText(SystemConfig.getVersion());
	}

}
