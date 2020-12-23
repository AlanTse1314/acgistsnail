@echo off

rem UTF-8
chcp 65001>nul

rem 软件名称
set project=snail
rem 软件版本
set version=1.6.0

rem 编译路径
set builder=.\builder\
rem 打包目录
set target=%builder%%project%\

rem 依赖路径
set lib=lib
rem 运行环境路径
set runtime=java

rem JAR文件
set jar=%project%.javafx-%version%.jar

rem BAT配置文件
set config=config.bat
rem BAT启动文件
set startup=startup.bat

rem 启动器文件
set exe=SnailLauncher.exe
rem 启动器配置
set ini=snail.ini
rem 启动器源码
set launcher=.\SnailLauncher\
rem 启动器编译路径
set launcherBuild=%launcher%build\
rem 启动器文件路径
set launcherExe=%launcherBuild%src\Release\%exe%
rem 启动器配置路径
set launcherIni=%launcher%src\%ini%

rem Java依赖模块
set modules="java.base,java.xml,java.desktop,java.net.http,jdk.crypto.ec,java.scripting,jdk.unsupported"
