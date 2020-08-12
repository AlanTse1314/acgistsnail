package com.acgist.snail.gui.javafx.theme;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.javafx.Themes;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.StringUtils;

import javafx.scene.paint.Color;

/**
 * <p>Windows系统主题</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
public final class WindowsTheme implements ITheme {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WindowsTheme.class);

	/**
	 * <p>Windows主题颜色PATH</p>
	 */
	private static final String THEME_COLOR_PATH = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\History\\Colors";
	/**
	 * <p>Windows主题颜色KEY</p>
	 */
	private static final String THEME_COLOR_KEY = "ColorHistory0";
	/**
	 * <p>Windows获取主题颜色命令</p>
	 */
	private static final String THEME_COLOR_COMMAND = "REG QUERY " + THEME_COLOR_PATH + " /v " + THEME_COLOR_KEY;
	
	private WindowsTheme() {
	}

	/**
	 * <p>新建Windows系统主题</p>
	 * 
	 * @return Windows系统主题
	 */
	public static final WindowsTheme newInstance() {
		return new WindowsTheme();
	}
	
	@Override
	public Color systemThemeColor() {
		String line;
		String color = null;
		Process process = null;
		OutputStream output = null;
		BufferedReader reader = null;
		try {
			process = Runtime.getRuntime().exec(THEME_COLOR_COMMAND);
			output = process.getOutputStream();
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if(StringUtils.startsWith(line, THEME_COLOR_KEY)) {
					final int index = line.indexOf("0x");
					color = line.substring(index).trim();
					break;
				}
			}
			return this.convertWindowsColor(color);
		} catch (Exception e) {
			LOGGER.error("获取Windows主题颜色异常", e);
		} finally {
			IoUtils.close(output);
			IoUtils.close(reader);
			if(process != null) {
				process.destroy();
			}
		}
		return Themes.DEFAULT_THEME_COLOR;
	}

	/**
	 * <p>Windows颜色转换</p>
	 * 
	 * @param colorValue 十六进制颜色字符：0xffffff、0xffffffff
	 * 
	 * @return 颜色
	 */
	private Color convertWindowsColor(String colorValue) {
		Color theme;
		if(colorValue == null) {
			theme = Themes.DEFAULT_THEME_COLOR;
		} else {
			final int value = (int) Long.parseLong(colorValue.substring(2), 16);
			final int alpha = (int) ((value >> 24) & 0xFF); // 透明度：可能不存在
			final int blud = (int) ((value >> 16) & 0xFF);
			final int green = (int) ((value >> 8) & 0xFF);
			final int red = (int) (value & 0xFF);
			final double opacity = alpha >= 255D ? 1D : alpha / 255D;
			if(alpha == 0) { // 没有透明度默认设置不透明
				theme = Color.rgb(red, green, blud);
			} else {
				theme = Color.rgb(red, green, blud, opacity);
			}
		}
		LOGGER.info("Windows系统主题颜色：{}-{}", colorValue, theme);
		return theme;
	}
	
}
