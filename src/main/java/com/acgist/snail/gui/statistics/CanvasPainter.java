package com.acgist.snail.gui.statistics;

import java.util.BitSet;
import java.util.Objects;

import com.acgist.snail.utils.NumberUtils;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * <p>位图工具</p>
 * <p>使用画布实现</p>
 * 
 * TODO：观察者实时更新
 * TODO：标记全部Piece和选择Piece和下载Piece
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class CanvasPainter {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(CanvasPainter.class);

	/**
	 * <p>默认填充高宽</p>
	 */
	private static final int DEFAULT_WH = 16;
	/**
	 * <p>默认列数</p>
	 */
	private static final int DEFAULT_COL = 50;
	
	/**
	 * <p>填充高宽</p>
	 */
	private final int wh;
	/**
	 * <p>列数</p>
	 */
	private final int col;
	/**
	 * <p>行数</p>
	 */
	private final int row;
	/**
	 * <p>数据长度</p>
	 */
	private final int length;
	/**
	 * <p>数据</p>
	 */
	private final BitSet bitSet;
	/**
	 * <p>存在填充颜色</p>
	 */
	private final Color fillColor;
	/**
	 * <p>没有填充颜色</p>
	 */
	private final Color noneColor;
	/**
	 * <p>边框颜色</p>
	 */
	private final Color borderColor;
	/**
	 * <p>背景颜色</p>
	 */
	private final Color background;
	/**
	 * <p>图片宽度</p>
	 */
	private int width;
	/**
	 * <p>图片高度</p>
	 */
	private int height;
	/**
	 * <p>画布</p>
	 */
	private Canvas canvas;
	/**
	 * <p>画笔</p>
	 */
	private GraphicsContext graphics;
	/**
	 * <p>边框高宽</p>
	 */
	private final int borderWh = 1;
	
	private CanvasPainter(BitSet bitSet) {
		this(bitSet.size(), bitSet);
	}

	private CanvasPainter(int length, BitSet bitSet) {
		this(DEFAULT_WH, DEFAULT_COL, length, bitSet);
	}
	
	private CanvasPainter(int wh, int col, int length, BitSet bitSet) {
		this(
			wh, col, length, bitSet,
			Color.rgb(0, 153, 204),
			Color.rgb(200, 200, 200),
			Color.BLACK, Color.WHITE
		);
	}
	
	private CanvasPainter(
		int wh, int col, int length, BitSet bitSet,
		Color fillColor, Color noneColor, Color borderColor, Color background
	) {
		this.wh = wh;
		this.col = col;
		this.row = NumberUtils.ceilDiv(length, col);
		this.fillColor = fillColor;
		this.noneColor = noneColor;
		this.borderColor = borderColor;
		this.background = background;
		this.length = length;
		this.bitSet = bitSet;
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param bitSet 数据
	 * 
	 * @return 工具
	 */
	public static final CanvasPainter newInstance(BitSet bitSet) {
		return new CanvasPainter(bitSet);
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param length 数据长度
	 * @param bitSet 数据
	 * 
	 * @return 工具
	 */
	public static final CanvasPainter newInstance(int length, BitSet bitSet) {
		return new CanvasPainter(length, bitSet);
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSet 数据
	 * 
	 * @return 工具
	 */
	public static final CanvasPainter newInstance(int wh, int col, int length, BitSet bitSet) {
		return new CanvasPainter(wh, col, length, bitSet);
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSet 数据
	 * @param fillColor 存在填充颜色
	 * @param noneColor 没有填充颜色
	 * @param borderColor 边框颜色
	 * @param background 背景颜色
	 * 
	 * @return 工具
	 */
	public static final CanvasPainter newInstance(
		int wh, int col, int length, BitSet bitSet,
		Color fillColor, Color noneColor, Color borderColor, Color background
	) {
		return new CanvasPainter(wh, col, length, bitSet, fillColor, noneColor, borderColor, background);
	}

	/**
	 * <p>开始画图</p>
	 * 
	 * @return {@code this}
	 */
	public CanvasPainter draw() {
		this.drawFill();
		return this;
	}
	
	/**
	 * <p>开始画图</p>
	 * 
	 * @param bitSet 数据
	 * 
	 * @return {@code this}
	 */
	public CanvasPainter draw(BitSet bitSet) {
		this.bitSet.or(bitSet);
		// 没有数据增加
		if(this.bitSet.cardinality() == bitSet.cardinality()) {
			return this;
		}
		this.drawFill();
		return this;
	}
	
	/**
	 * <p>开始画图</p>
	 * 
	 * @param index 数据索引
	 * 
	 * @return {@code this}
	 */
	public CanvasPainter draw(int index) {
		// 已经包含数据
		if(this.bitSet.get(index)) {
			return this;
		}
		this.bitSet.set(index);
		this.graphics.save();
		final int wh = this.wh + this.borderWh;
		final int row = index / this.col;
		final int col = index % this.col;
		this.drawFill(index, col, row, wh);
		this.graphics.restore();
		return this;
	}
	
	/**
	 * <p>创建画布、画笔，画出背景和边框。</p>
	 * 
	 * @return {@code this}
	 */
	public CanvasPainter build() {
		return this.build(null);
	}
	
	/**
	 * <p>创建画布和画笔，画出背景和边框。</p>
	 * 
	 * @param canvas 画布
	 * 
	 * @return {@code this}
	 */
	public CanvasPainter build(Canvas canvas) {
		if(this.canvas == null) {
			// 计算高宽
			this.width = this.col * (this.wh + this.borderWh) + this.borderWh; // 列数 * (宽度 + 边框) + 右边框
			this.height = this.row * (this.wh + this.borderWh) + this.borderWh; // 行数 * (高度 + 边框) + 底边框
			// 创建画布
			this.canvas = new Canvas(this.width, this.height);
		} else {
			this.canvas = canvas;
		}
		// 创建画笔
		this.graphics = this.canvas.getGraphicsContext2D();
		this.drawBackground();
		this.drawBorder();
		return this;
	}

	/**
	 * <p>背景</p>
	 */
	private void drawBackground() {
		this.graphics.save();
		// 清空背景
		this.graphics.clearRect(0, 0, this.width, this.height);
		// 背景
		this.graphics.setFill(this.background);
		this.graphics.fillRect(0, 0, this.width, this.height);
		this.graphics.restore();
	}
	
	/**
	 * <p>边框</p>
	 */
	private void drawBorder() {
		this.graphics.save();
		this.graphics.setStroke(this.borderColor);
		this.graphics.setLineWidth(this.borderWh);
		// 列
		final int width = this.wh + this.borderWh;
		int x = 0;
		for (int index = 0; index < this.col; index++) {
			x = index * width;
			this.graphics.strokeLine(x, 0, x, this.height);
		}
		// 右边框
		this.graphics.strokeLine(this.width - this.borderWh, 0, this.width - this.borderWh, this.height);
		// 行
		final int height = this.wh + this.borderWh;
		int y = 0;
		for (int index = 0; index < this.row; index++) {
			y = index * height;
			this.graphics.strokeLine(0, y, this.width, y);
		}
		// 底边框
		this.graphics.strokeLine(0, this.height - this.borderWh, this.width, this.height - this.borderWh);
		this.graphics.restore();
	}
	
	/**
	 * <p>填充数据</p>
	 */
	private void drawFill() {
		this.graphics.save();
		int col, row;
		final int wh = this.wh + this.borderWh;
		for (int index = 0; index < this.length; index++) {
			row = index / this.col;
			col = index % this.col;
			this.drawFill(index, col, row, wh);
		}
		this.graphics.restore();
	}
	
	/**
	 * <p>填充数据</p>
	 * 
	 * @param index 数据索引
	 * @param col 列
	 * @param row 行
	 * @param wh 填充高宽
	 */
	private void drawFill(int index, int col, int row, int wh) {
		if(this.bitSet.get(index)) {
			this.graphics.setFill(this.fillColor);
			this.graphics.fillRect(col * wh + this.borderWh, row * wh + this.borderWh, this.wh, this.wh);
		} else {
			this.graphics.setFill(this.noneColor);
			this.graphics.fillRect(col * wh + this.borderWh, row * wh + this.borderWh, this.wh, this.wh);
		}
	}
	
	/**
	 * <p>获取画布</p>
	 * 
	 * @return 画布
	 */
	public Canvas canvas() {
		Objects.requireNonNull(this.canvas, "没有创建画布");
		return this.canvas;
	}
	
}
