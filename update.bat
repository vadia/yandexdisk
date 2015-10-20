@echo off
chcp 1251
echo Pull from repository

IF EXIST .\.hg  GOTO PULL_HG
IF EXIST .\.git GOTO PULL_GIT

:PULL_HG
call hg pull -u
if not "%ERRORLEVEL%" == "0" GOTO FAIL
GOTO PULL_SUCCESS

:PULL_GIT
call git pull
if not "%ERRORLEVEL%" == "0" GOTO FAIL
GOTO PULL_SUCCESS

:PULL_SUCCESS
IF EXIST make.bat GOTO MAKE
GOTO SUCCESS

:MAKE
call make.bat
exit

:SUCCESS
echo ********************************************
echo ************* UPDATE SUCCESS ***************
echo ********************************************
IF [%1] == [] PAUSE
EXIT /b

:FAIL
echo ********************************************
echo ************** PULL FAIL *******************
echo ********************************************
IF [%1] == [] PAUSE
EXIT /b