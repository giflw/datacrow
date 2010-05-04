@echo off
@rem =====================================
@rem DOS Batch file to invoke the compiler
@rem =====================================

call "C:/Program Files/Development/tools/izpack/bin/compile.bat" installer.xml -b ../
"C:/Program Files/Development/tools/jsmooth/jsmooth/jsmoothcmd" ./installer.jsmooth
@echo on