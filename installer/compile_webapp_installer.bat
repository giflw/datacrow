@echo off
@rem =====================================
@rem DOS Batch file to invoke the compiler
@rem =====================================

call D:/development/tools/izpack/bin/compile.bat installer_webapp.xml -b ../
D:/development/tools/jsmooth/jsmooth/jsmoothcmd ./installer_webapp.jsmooth
@echo on

