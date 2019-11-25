@echo off

rem ���������ļ�
call config.bat

echo ��ʼ������Ŀ��%project%��

rem ȷ�ϰ汾��Ϣ��pom.xml��builder/config.bat��SnailLauncher/src/snail.ini��src/main/resources/config/system.properties
set /p input=��ȷ�������ļ��汾��Ϣ�Ƿ�һ�£�Y/N����
if /i %input%==Y (
  echo -----------------------------------------------
  echo ������Ŀ
  echo -----------------------------------------------
) else (
  exit
)

rem ����ļ�
call clean.bat

cd ..\

echo -----------------------------------------------
echo ������Ŀ
echo -----------------------------------------------
call mvn clean package -P release -D skipTests

echo -----------------------------------------------
echo ���л���
echo -----------------------------------------------
call jlink --add-modules %modules% --output %target%%runtime%

echo -----------------------------------------------
echo ����������
echo -----------------------------------------------
cd %launcher%
call mkdir build
call cd build
call cmake -G "Visual Studio 11 2012 Win64" ..
call cmake --build . --config Release

cd ..\..\

echo -----------------------------------------------
echo �����ļ�
echo -----------------------------------------------
call xcopy /S /Q .\target\%lib%\* %target%%lib%\
call copy .\target\%jar% %target%
call copy %launcherExe% %target%%exe%
call copy %launcherIni% %target%%ini%
call copy %builder%%config% %target%%config%
call copy %builder%%startup% %target%%startup%

cd %builder%

echo -----------------------------------------------
echo �����ɹ�
echo -----------------------------------------------

exit