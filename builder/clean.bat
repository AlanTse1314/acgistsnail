@echo off

call config.bat

rem �û�ȷ��
set /p input=��ȷ����������ɵ����ݣ�Y/N����
if /i %input%==Y (echo ��ʼ���) else (exit)

rem ����ļ�
echo -----------------------------------------------
echo ����ļ�
echo -----------------------------------------------
if exist %jar% del /F /A /Q %jar%
if exist %exe% del /F /A /Q %exe%
if exist %lib% rd /S /Q %lib%
if exist %logs% rd /S /Q %logs%
rem if exist %database% rd /S /Q %database%
if exist %runtime% rd /S /Q %runtime%