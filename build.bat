rd _classes /S /Q
del datacrow.jar
call ant
chmod -R +r+w datacrow.jar
chmod -R +r+w ./plugins/*
chmod -R +r+w ./services/*