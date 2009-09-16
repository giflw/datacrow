rd ..\..\release /S /Q
md ..\..\release
md ..\..\release\datacrow
rd webapp\datacrow\mediaimages /S /Q
rd data /S /Q
rd _build /S /Q
call build.bat
del installer\*.jar
del installer\*.exe
cd ..\services
call build.bat
cd ..\datacrow
del plugins\*.class
copy ..\plugins\_build\\plugins\*.* .\plugins\ /Y
xcopy * ..\..\release\datacrow /s /EXCLUDE:release.exclude
cd ..\..\release\datacrow
rd _classes /S /Q
rd homepage /S /Q
rd logo_design /S /Q
rd documentation /S /Q
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
cd ..
7z a -tZip datacrow_x_source .\datacrow
7z a -tZip datacrow_x_javadoc .\javadoc
rd javadoc /S /Q
cd datacrow
call build.bat
rd _classes /S /Q
rd _source /S /Q
del manifest.mf
del build.bat
del build.xml
del *.classpath
del *.project
cd ..
7z a -tZip datacrow_x_zipped .\datacrow
7z a -tZip datacrow_x_installer installer.jar installer.sh installer.txt
7z a -tZip datacrow_x_windows_installer installer.jar setup.exe
del installer.sh
del installer.txt
del installer.jar
del setup.exe
pause