@echo off

rem �������
set project=snail
rem ����汾
set version=1.2.0

rem ����·��
set builder=.\builder\
rem ���Ŀ¼
set target=%builder%%project%\

rem �������ļ���
set lib=lib
rem ���л����ļ���
set runtime=java

rem JAR�ļ�
set jar=%project%-%version%.jar

rem BAT�����ļ�
set config=config.bat
rem BAT�����ļ�
set startup=startup.bat

rem �����ļ�
set exe=SnailLauncher.exe
rem �����ļ�����
set ini=snail.ini
rem �����ļ�·��
set launcherExe=.\SnailLauncher\%exe%
rem �����ļ�����·��
set launcherIni=.\SnailLauncher\%ini%

rem JAVA����ģ�飺jdeps --list-deps *.jar
set modules="java.sql,java.base,java.desktop,java.net.http,java.naming,java.scripting,java.management,jdk.crypto.ec,jdk.unsupported"
