@echo off

rem ���������ļ�
call config.bat

echo ��ʼ������Ŀ��%project%��

rem ȷ�ϰ汾��Ϣ
set /p input=��ȷ�����������ļ���pom.xml��snail.ini��config.bat��system.properties���汾��Ϣһ�£�Y/N����
if /i %input%==Y (echo ��ʼ������Ŀ) else (exit)

rem ����ļ�
call clean.bat

cd ..\

echo -----------------------------------------------
echo �����Ŀ
echo -----------------------------------------------
call mvn clean package -q -P release -D skipTests

echo -----------------------------------------------
echo �����ļ�
echo -----------------------------------------------
call xcopy /S /Q .\target\%lib%\* %target%%lib%\
call copy .\target\%jar% %target%
call copy %launcherExe% %target%%exe%
call copy %launcherIni% %target%%ini%
call copy %builder%%config% %target%%config%
call copy %builder%%startup% %target%%startup%

echo -----------------------------------------------
echo ���л���
echo -----------------------------------------------
call jlink --add-modules %modules% --output %target%%runtime%

cd %builder%

echo -----------------------------------------------
echo �����ɹ�
echo -----------------------------------------------
