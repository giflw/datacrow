@echo off
@rem =====================================
@rem DOS Batch file to invoke the compiler
@rem =====================================

call "C:/Users/RJ/Data/Development/tools/izpack/bin/compile.bat" installer.xml -b ../
"C:/Users/RJ/Data/Development/tools/jsmooth/jsmooth/jsmoothcmd" ./installer.jsmooth
@echo on
