rd ..\..\release /S /Q
md ..\..\release
md ..\..\release\datacrow
rd webapp\datacrow\mediaimages /S /Q
rd data /S /Q
rd _build /S /Q
rd _classes /S /Q
del datacrow.jar
call build.bat
del installer\*.jar
del installer\*.exe
rd ..\services\_build /S /Q
rd ..\services\_classes /S /Q
cd ..\services
call build.bat
copy standard_services_pack.jar .\services\ /Y
cd ..\datacrow
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
cd ..\..
7z a -tZip datacrow_source .\datacrow
7z a -tZip datacrow_javadoc .\javadoc
rd javadoc /S /Q
cd datacrow
call build.bat
rd _source /S /Q
rd _classes /S /Q
cd ..
7z a -tZip datacrow_zipped .\datacrow
7z a -tZip installer installer.jar installer.sh installer.txt
del installer.jar
pause


