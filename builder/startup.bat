@echo off

rem ���������ļ�
call config.bat

echo ������Ŀ��%project%��

rem ���û�������
set path=.\%runtime%\bin

rem ��������
call start "snail" javaw -server -XX:NewRatio=2 -XX:SurvivorRatio=2 -Xms128m -Xmx256m -jar %jar%

exit
