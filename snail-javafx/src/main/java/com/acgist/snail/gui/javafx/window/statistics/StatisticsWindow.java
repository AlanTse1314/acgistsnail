package com.acgist.snail.gui.javafx.window.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.javafx.window.Window;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * <p>统计窗口</p>
 * 
 * @author acgist
 */
public final class StatisticsWindow extends Window<StatisticsController> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsWindow.class);
	
	private static final StatisticsWindow INSTANCE;
	
	public static final StatisticsWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		LOGGER.debug("初始化统计窗口");
		INSTANCE = new StatisticsWindow();
	}
	
	private StatisticsWindow() {
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, "统计", 840, 640, "/fxml/statistics.fxml", Modality.APPLICATION_MODAL);
		this.dialogWindow();
		this.windowHidden();
	}
	
	@Override
	public void show() {
		this.controller.statistics();
		super.show();
	}

	/**
	 * @param piecePos 指定下载Piece索引
	 * 
	 * @see StatisticsController#piecePos(int)
	 */
	public void piecePos(int index) {
		this.controller.piecePos(index);
	}
	
	/**
	 * <p>窗口隐藏：释放资源</p>
	 */
	private void windowHidden() {
		this.stage.addEventFilter(WindowEvent.WINDOW_HIDDEN, event -> {
			this.controller.release();
		});
	}
	
}
