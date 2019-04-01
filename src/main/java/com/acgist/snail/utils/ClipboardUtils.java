package com.acgist.snail.utils;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * utils - 剪切板
 */
public class ClipboardUtils {

	/**
	 * 剪切板拷贝
	 */
	public static final void copy(String value) {
		final ClipboardContent content = new ClipboardContent();
		content.putString(value);
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		clipboard.setContent(content);
	}
	
}
