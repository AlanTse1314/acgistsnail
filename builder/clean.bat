@echo off

call config.bat

rem �û�ȷ��
set /p input=��ȷ��ɾ��ȫ�����ݣ�Y/N����
if /i %input%==Y (echo ��ʼɾ��) else (exit)

rem ɾ���ļ�
echo -----------------------------------------------
echo ɾ���ļ�
echo -----------------------------------------------
if exist %jar% del /F /A /Q %jar%
if exist %exe% del /F /A /Q %exe%
if exist %lib% rd /S /Q %lib%
if exist %logs% rd /S /Q %logs%
rem if exist %database% rd /S /Q %database%
if exist %runtime% rd /S /Q %runtime%