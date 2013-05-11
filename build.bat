@echo off
echo Setting JAVA_HOME
set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_38
echo setting PATH
set PATH=C:\Program Files\Java\jdk1.6.0_38\bin;%PATH%
echo Display java version
java -version
pause
rd _classes /S /Q
del datacrow.jar
call ant
chmod -R +r+w datacrow.jar
chmod -R +r+w ./plugins/*
chmod -R +r+w ./services/*