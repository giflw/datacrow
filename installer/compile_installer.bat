@echo off
@rem =====================================
@rem DOS Batch file to invoke the compiler
@rem =====================================

call D:/development/tools/izpack/bin/compile.bat installer.xml -b ../
D:/development/tools/jsmooth/jsmooth/jsmoothcmd D:\development\projects\datacrow\installer\installer.jsmooth

pause
@echo on

