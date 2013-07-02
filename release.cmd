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
del installer\*.exe
cd ..\services
call build.bat
cd ..\datacrow
"C:\Program Files\Jar2Exe Wizard\j2ewiz" /jar C:\Users\RJ\Data\Development\projects\datacrow\datacrow.jar /o C:\Users\RJ\Data\Development\projects\datacrow\datacrow32bit.exe /m net.datacrow.core.DataCrow /type windows /minjre 1.6 /platform windows /checksum /icon "C:\Users\RJ\Data\Development\projects\datacrow\icons\datacrow.ico, 0" /pv 3,0,0,1 /fv 3,0,0,1 /ve ProductVersion=3,0 /ve "ProductName=Data Crow" /ve "LegalCopyright=Copyright (c) 2013 and beyond" /ve "SpecialBuild=3, 0" /ve "FileVersion=3, 0" /ve "FileDescription=Data Crow" /ve "LegalTrademarks=Trade marks" /ve "InternalName=3, 0" /ve "CompanyName=Robert Jan van der Waals" /config "option -Xms512m"
"C:\Program Files\Jar2Exe Wizard\j2ewiz" /jar C:\Users\RJ\Data\Development\projects\datacrow\datacrow.jar /o C:\Users\RJ\Data\Development\projects\datacrow\datacrow32bit /m net.datacrow.core.DataCrow /type console /minjre 1.6 /platform linux /checksum /config "option -Xms512m"
"C:\Program Files\Jar2Exe Wizard\j2ewiz" /jar C:\Users\RJ\Data\Development\projects\datacrow\datacrow.jar /o C:\Users\RJ\Data\Development\projects\datacrow\datacrow64bit.exe /m net.datacrow.core.DataCrow /type windows /minjre 1.6 /platform windows /checksum /amd64 /icon "C:\Users\RJ\Data\Development\projects\datacrow\icons\datacrow.ico, 0" /pv 3,0,0,1 /fv 3,0,0,1 /ve ProductVersion=3,0 /ve "ProductName=Data Crow" /ve "LegalCopyright=Copyright (c) 2013 and beyond" /ve "SpecialBuild=3, 0" /ve "FileVersion=3, 0" /ve "FileDescription=Data Crow" /ve "LegalTrademarks=Trade marks" /ve "InternalName=3, 0" /ve "CompanyName=Robert Jan van der Waals" /config "option -Xms512m"
"C:\Program Files\Jar2Exe Wizard\j2ewiz" /jar C:\Users\RJ\Data\Development\projects\datacrow\datacrow.jar /o C:\Users\RJ\Data\Development\projects\datacrow\datacrow64bit /m net.datacrow.core.DataCrow /type console /minjre 1.6 /platform linux /checksum /amd64 /config "option -Xms512m"
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
cd installer
call compile_installer.bat
call compile_webapp_installer.bat
move *.jar ..\..\
move *.exe ..\..\
move installer.sh ..\..\
move installer.txt ..\..\
rd installer /S /Q
cd ..
del *.j2e
del release.cmd
del release.exclude
del build-javadoc.bat
rd _classes /S /Q
rd temp /S /Q
cd ..
7z a -tZip datacrow_3_10_2_source .\datacrow
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
7z a -tZip datacrow_3_10_2_zipped .\datacrow
7z a -tZip datacrow_3_10_2_installer installer.jar installer.sh installer.txt
7z a -tZip datacrow_3_10_2_windows_installer installer.jar setup.exe
del installer.sh
del installer.txt
del installer.jar
del setup.exe
pause