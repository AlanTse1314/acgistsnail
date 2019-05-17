package com.acgist.snail.gui.about;

import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.BrowseUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

/**
 * 关于窗口控制器
 * 
 * @author acgist
 * @since 1.0.0
 */
public class AboutController implements Initializable {
	
	@FXML
	private Text name;
	@FXML
	private GridPane root;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		final StringBuilder name = new StringBuilder();
		name.append("名称：").append(SystemConfig.getName())
			.append("（").append(SystemConfig.getNameEn()).append("）")
			.append(SystemConfig.getVersion());
		this.name.setText(name.toString());
	}
	
	/**
	 * 作者按钮
	 */
	@FXML
	public void handleAuthorAction(ActionEvent event) {
		BrowseUtils.open(SystemConfig.getAuthor());
	}
	
	/**
	 * 源码按钮
	 */
	@FXML
	public void handleSourceAction(ActionEvent event) {
		BrowseUtils.open(SystemConfig.getSource());
	}
	
	/**
	 * 支持按钮
	 */
	@FXML
	public void handleSupportAction(ActionEvent event) {
		BrowseUtils.open(SystemConfig.getSupport());
	}

}
