/**
 * <h1>Sanil（蜗牛）下载工具</h1>
 * <p>支持下载协议：BT、FTP、HTTP。</p>
 * <p>方法注解（author、since）没有时，默认使用类注解。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
open module com.acgist.snail {

	/*
	 * 限定导出
	 */
	exports com.acgist.main;

	/*
	 * opens：开放包（反射时需要，使用open module不需要此配置）<br>
	 * opens com.acgist.main;
	 */

	/*
	 * Java
	 */
	requires java.sql;
	requires java.xml;
	requires java.base;
	requires java.desktop;
	requires java.net.http;

	/*
	 * jdeps（jdeps --list-deps *.jar）分析出来的依赖：
	 * java.sql
	 * java.xml
	 * java.base
	 * java.naming
	 * java.desktop
	 * java.logging
	 * java.scripting
	 * java.management
	 * jdk.unsupported
	 */

	/*
	 * JavaFX
	 */
	requires transitive javafx.fxml;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;

	/*
	 * 其他：数据库、日志、JSON
	 */
	requires transitive org.slf4j;
	requires transitive com.h2database;
	requires transitive ch.qos.logback.core;
	requires transitive ch.qos.logback.classic;

}