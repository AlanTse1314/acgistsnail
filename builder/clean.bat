@echo off

rem ���������ļ�
call config.bat

rem �û�ȷ��
set /p input=��ȷ���Ƿ�����������ļ���Y/N����
if /i %input%==Y (echo ��ʼ���) else (exit)

echo -----------------------------------------------
echo ����ļ�
echo -----------------------------------------------
if exist %project% rd /S /Q %project%

cd ..

echo -----------------------------------------------
echo ���Maven
echo -----------------------------------------------
call mvn clean -D skipTests

echo -----------------------------------------------
echo ���������
echo -----------------------------------------------
if exist %launcherBuild% rd /S /Q %launcherBuild%

cd %builder%