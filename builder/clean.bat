@echo off

rem ���������ļ�
call config.bat

rem �û�ȷ��
set /p input=��ȷ����������ɵ����ݣ�Y/N����
if /i %input%==Y (echo ��ʼ���) else (exit)

echo -----------------------------------------------
echo ����ļ�
echo -----------------------------------------------
if exist %project% rd /S /Q %project%
