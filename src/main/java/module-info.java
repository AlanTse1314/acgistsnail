/**
 * 模块化<br>
 * open：开放模块
 */
open module snail {
	/**
	 * 限定导出
	 **/
	exports com.acgist.main;
	exports com.acgist.snail.window.about;
	exports com.acgist.snail.pojo.entity;
	
	/**
	 * opens：开放包（深层反射），使用open module不需要此配置。<br>
	 * opens com.acgist.main
	 */

	/**
	 * Java
	 */
	requires java.sql;
	requires java.base;
	requires java.desktop;
	requires java.instrument;
	
	/**
	 * jdeps 分析出来的依赖
	 */
//	requires java.logging;
//	requires java.scripting;
	
	/**
	 * JavaFX
	 */
	requires transitive javafx.fxml;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	
	/**
	 * 其他依赖
	 */
	requires transitive slf4j.api; // TODO：升级SLF4J依赖
	requires transitive org.apache.commons.lang3;
	requires transitive com.fasterxml.jackson.core;
	requires transitive com.fasterxml.jackson.databind;
	requires transitive com.fasterxml.jackson.annotation;
}