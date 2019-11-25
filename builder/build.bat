@echo off

rem ���������ļ�
call config.bat

echo ��ʼ������Ŀ��%project%��

rem ȷ�ϰ汾��Ϣ
set /p input=��ȷ�����������ļ���pom.xml��SnailLauncher/src/snail.ini��builder/config.bat��src/main/resources/config/system.properties���汾��Ϣһ�£�Y/N����
if /i %input%==Y (
  echo -----------------------------------------------
  echo ��ʼ������Ŀ
  echo -----------------------------------------------
) else (
  exit
)

rem ����ļ�
call clean.bat

cd ..\

echo -----------------------------------------------
echo �����Ŀ
echo -----------------------------------------------
call mvn clean package -P release -D skipTests

echo -----------------------------------------------
echo ���л���
echo -----------------------------------------------
call jlink --add-modules %modules% --output %target%%runtime%

echo -----------------------------------------------
echo ִ���ļ�
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