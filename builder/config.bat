@echo off

rem �������
set project=snail
rem ����汾
set version=1.2.1

rem ����·��
set builder=.\builder\
rem ���Ŀ¼
set target=%builder%%project%\

rem ����·��
set lib=lib
rem ���л���·��
set runtime=java

rem JAR�ļ�
set jar=%project%-%version%.jar

rem BAT�����ļ�
set config=config.bat
rem BAT�����ļ�
set startup=startup.bat

rem �������ļ�
set exe=SnailLauncher.exe
rem ����������
set ini=snail.ini
rem ������Դ��
set launcher=.\SnailLauncher\
rem ����������·��
set launcherBuild=.\SnailLauncher\build\
rem �������ļ�·��
set launcherExe=%launcherBuild%src\Release\%exe%
rem ����������·��
set launcherIni=%launcher%src\%ini%

rem Java����ģ��
set modules="java.xml,java.sql,java.base,java.desktop,java.naming,java.compiler,java.logging,java.scripting,java.instrument,java.management,java.net.http,java.transaction.xa,jdk.crypto.ec,jdk.unsupported"
