@echo off

if "%JAVA_OPTS%" == "" goto DEFAULT_JAVA_OPTS

:INVOKE
echo JAVA_HOME=%JAVA_HOME%
echo JAVA_OPTS=%JAVA_OPTS%
echo ROCK_HOME=%ROCK_HOME%

if "%ROCK_HOME%" == "" goto ROCK_HOME_NOT_SET

setlocal ENABLEDELAYEDEXPANSION

set ROCK_DIST=%~dp0..
echo ROCK_DIST=%ROCK_DIST%

set ROCK_LOG=%ROCK_HOME%\logs
IF NOT EXIST "%ROCK_LOG%" mkdir "%ROCK_LOG%"
echo ROCK_LOG=%ROCK_LOG%

set CLASSPATH=%ROCK_HOME%\conf;%ROCK_DIST%\lib\*

set JAVA_DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n

rem Add %JAVA_DEBUG% to this line to enable remote JVM debugging (for developers)
java %JAVA_OPTS% -cp "%CLASSPATH%" -DROCK_HOME="%ROCK_HOME%" -DROCK_DIST=%ROCK_DIST% org.springframework.boot.loader.launch.JarLauncher %*
goto :END

:DEFAULT_JAVA_OPTS
set JAVA_OPTS=-Xmx128M -XX:+UseG1GC
goto :INVOKE

:JAVA_HOME_NOT_SET
echo JAVA_HOME not set
goto :END

:ROCK_HOME_NOT_SET
echo ROCK_HOME not set
goto :END

:END
