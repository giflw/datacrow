rd ..\..\release /S /Q
md ..\..\release
md ..\..\release\source
rd webapp\datacrow\mediaimages /S /Q
rd data /S /Q
rd _build /S /Q
rd _classes /S /Q
del datacrow.jar
del installer\*.jar
del installer\*.exe
xcopy * ..\..\release\source /s /EXCLUDE:release.exclude
cd ..\..\release\source
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
cd ..\..
7z a -tZip .\javadoc javadoc
rd javadoc /S /Q
7z a -tZip .\installer.jar installer
7z a -tZip .\installer.sh installer
7z a -tZip .\installer.txt installer
java -jar installer.jar
pause


