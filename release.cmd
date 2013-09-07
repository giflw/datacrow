@echo off
echo Setting JAVA_HOME
set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_38
echo setting PATH
set PATH=C:\Program Files\Java\jdk1.6.0_38\bin;%PATH%
echo Display java version
java -version
pause
cd C:\Users\RJ\Data\Development\projects\datacrow
rd ..\..\release /S /Q
md ..\..\release
md ..\..\release\datacrow
rd _build /S /Q
call build.bat
cd help
call create_index.bat
cd ..
del installer\*.jar
cd ..\services
call build.bat
cd ..\datacrow
del plugins\*.class
copy ..\plugins\_build\plugins\*.* .\plugins\ /Y
xcopy * ..\..\release\datacrow /E /o /EXCLUDE:release.exclude
cd ..\..\release\datacrow
chmod -R +r+w *
rd _classes /S /Q
rd homepage /S /Q
rd logo_design /S /Q
rd documentation /S /Q
chmod -R +r+w ./*.*
call build.bat
cd installer
call compile_installer.bat
call compile_webapp_installer.bat
move *.jar ..\..\
move *.exe ..\..\
move installer.sh ..\..\
move installer.txt ..\..\
move readme.txt ..\..\
rd installer /S /Q
cd ..
del *.j2e
del release.cmd
del release.exclude
del build-javadoc.bat
rd _classes /S /Q
rd temp /S /Q
cd ..
7z a -tZip datacrow_3_12_3_source .\datacrow
cd datacrow
call build.bat
rd _classes /S /Q
rd _source /S /Q
rd installer /S /Q
rd .settings /S /Q
del manifest.mf
del build.bat
del build.xml
del *.classpath
del *.project
cd ..
7z a -tZip datacrow_3_12_3_zipped .\datacrow
7z a -tZip datacrow_3_12_3_installer installer.jar installer.sh installer.txt
7z a -tZip datacrow_3_12_3_windows_installer installer.jar setup32bit.exe setup64bit.exe readme.txt
del installer.sh
del installer.txt
del installer.jar
del setup32bit.exe
del setup64bit.exe
pause