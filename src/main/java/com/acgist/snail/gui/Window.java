package com.acgist.snail.gui;

import javafx.application.Application;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * 抽象窗口
 */
public abstract class Window<T extends Initializable> extends Application {

	protected T controller;
	protected Stage stage;
	
	public Window() {
		stage = new Stage();
	}
	
	/**
	 * 设置ICON
	 */
	protected void icon() {
		stage.getIcons().add(new Image("/image/logo.png"));
	}
	
	/**
	 * ESC隐藏窗口
	 */
	protected void esc() {
		stage.addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
			if(event.getCode() == KeyCode.ESCAPE) {
				stage.hide();
			}
		});
	}
	
	/**
	 * 禁止改变窗口大小
	 */
	protected void disableResize() {
		stage.setResizable(false);
	}
	
	/**
	 * 设置通用信息
	 */
	protected void dialogWindow() {
		icon();
		esc();
	}
	
	/**
	 * 显示窗口
	 */
	public void show() {
		stage.show();
	}
	
	/**
	 * 显示窗口
	 */
	public void showAndWait() {
		stage.showAndWait();
	}
	
	/**
	 * 隐藏窗口
	 */
	public void hide() {
		stage.hide();
	}
	
	/**
	 * 窗口是否显示
	 */
	public boolean isShowing() {
		return stage.isShowing();
	}
	
	/**
	 * 返回容器
	 */
	public Stage stage() {
		return this.stage;
	}
	
	/**
	 * 获取控制器
	 */
	public T controller() {
		return controller;
	}
	
}
