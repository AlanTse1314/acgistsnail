@echo off

call config.bat

echo ��ʼ������Ŀ��%project%��
echo ��ȷ��MAVEN�汾��config.bat��snail.ini���������ð汾��һ��

call clean.bat

cd ..\

rem �����Ŀ
echo -----------------------------------------------
echo �����Ŀ
echo -----------------------------------------------
call mvn clean package -q -Prelease -DskipTests
call xcopy /S /Q .\target\%lib%\* %builder%%lib%\*
call copy .\target\%jar% %builder%
call copy %launcher% %builder%%exe%
call copy %launcherIni% %builder%%ini%

rem ����JAVA���л���
rem ��ѯ�������jdeps --list-deps *.jar
echo -----------------------------------------------
echo ����JAVA���л���
echo -----------------------------------------------
call jlink --add-modules %modules% --output %builder%%runtime%

cd %builder%