@echo off
@rem =====================================
@rem DOS Batch file to invoke the compiler
@rem =====================================

call "C:/Users/rwaals/Documents/Development/tools/izpack/bin/compile.bat" installer.xml -b ../
"C:/Users/rwaals/Documents/Development/tools/jsmooth/jsmooth/jsmoothcmd" ./installer.jsmooth
@echo on