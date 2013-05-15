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
pause
del installer\*.jar
del installer\*.exe
cd ..\services
call build.bat
cd ..\datacrow
del plugins\*.class
copy ..\plugins\_build\\plugins\*.* .\plugins\ /Y
xcopy * ..\..\release\datacrow /E /o /EXCLUDE:release.exclude
cd ..\..\release\datacrow
chmod -R +r+w *
rd _classes /S /Q
rd homepage /S /Q
rd logo_design /S /Q
rd documentation /S /Q
chmod -R +r+w ./*.*
call build.bat
call build-javadoc.bat
cd installer
call compile_installer.bat
call compile_webapp_installer.bat
move *.jar ..\..\
move *.exe ..\..\
move installer.sh ..\..\
move installer.txt ..\..\
rd installer /S /Q
cd ..
del release.cmd
del release.exclude
del build-javadoc.bat
rd _classes /S /Q
rd temp /S /Q
cd ..
7z a -tZip datacrow_3_9_26_source .\datacrow
7z a -tZip datacrow_3_9_26_javadoc .\javadoc
rd javadoc /S /Q
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
7z a -tZip datacrow_3_9_26_zipped .\datacrow
7z a -tZip datacrow_3_9_26_installer installer.jar installer.sh installer.txt
7z a -tZip datacrow_3_9_26_windows_installer installer.jar setup.exe
del installer.sh
del installer.txt
del installer.jar
del setup.exe
pause