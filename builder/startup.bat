@echo off

rem ���������ļ�
call config.bat

echo ��ʼ������Ŀ��%project%��

rem ���û�������
set path=.\%runtime%\bin

rem ��������
call start "snail" javaw -server -Xms256m -Xmx256m -jar %jar%

exit
