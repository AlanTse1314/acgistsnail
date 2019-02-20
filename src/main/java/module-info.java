/**
 * 模块化<br>
 * open：开放模块
 */
open module snail {
	/**
	 * 限定导出
	 **/
	exports com.acgist.main;
	exports com.acgist.snail.repository;
	exports com.acgist.snail.window.about;
	exports com.acgist.snail.pojo.entity;
	exports com.acgist.snail.repository.impl;
	
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
	requires java.annotation;
	requires java.persistence;
	
	/**
	 * jdeps 分析出来的依赖
	 */
//	requires java.xml;
//	requires java.rmi;
//	requires java.prefs;
//	requires java.naming;
//	requires java.logging;
//	requires java.scripting;
//	requires java.management;
//	requires java.sql.rowset;
//	requires java.datatransfer;
//	requires java.transaction.xa;
//
//	requires jdk.jdi;
//	requires jdk.attach;
//	requires jdk.httpserver;
//	requires jdk.unsupported;
	
	/**
	 * JavaFX
	 */
	requires transitive javafx.fxml;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	
	/**
	 * Spring
	 */
	requires transitive spring.tx;
	requires transitive spring.boot;
	requires transitive spring.core;
	requires transitive spring.beans;
	requires transitive spring.context;
	requires transitive spring.data.jpa;
	requires transitive spring.data.commons;
	requires transitive spring.boot.autoconfigure;
	
	/**
	 * 其他依赖
	 */
	requires transitive slf4j.api; // TODO：升级SLF4J依赖
	requires transitive org.hibernate.orm.core;
	requires transitive org.apache.commons.lang3;
}