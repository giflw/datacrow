@echo off
@rem =====================================
@rem DOS Batch file to invoke the compiler
@rem =====================================

D:/development/tools/izpack/bin/compile.bat installer_webapp.xml -b ../
D:/development/tools/jsmooth/jsmooth/jsmoothcmd D:\development\projects\datacrow\installer\installer_webapp.jsmooth

pause
@echo on

