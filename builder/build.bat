@echo off

call config.bat

echo ��ʼ������Ŀ%project%

cd ..\

rem ɾ�����ļ�
echo -----------------------------------------------
echo ɾ�����ļ�
echo -----------------------------------------------
del /F /A /Q %builder%%jar%
rd /S /Q %builder%%runtime%

rem ���JAR
echo -----------------------------------------------
echo ���JAR
echo -----------------------------------------------
call mvn clean package -DskipTests
call copy .\target\%jar% %builder%

rem ����JAVA���л���
rem ��ѯ�������jdeps --list-deps *.jar
echo -----------------------------------------------
echo ����JAVA���л���
echo -----------------------------------------------
call jlink --add-modules "java.sql,java.base,java.desktop,java.instrument,java.xml,java.rmi,java.prefs,java.naming,java.logging,java.scripting,java.management,java.sql.rowset,java.datatransfer,java.transaction.xa,jdk.jdi,jdk.attach,jdk.httpserver,jdk.unsupported" --output %builder%%runtime%

cd %builder%