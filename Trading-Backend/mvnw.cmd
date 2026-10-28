@echo off
setlocal
set "DIR=%~dp0"
if exist "%DIR%.mvn-bin\apache-maven-3.9.6\bin\mvn.cmd" (
    "%DIR%.mvn-bin\apache-maven-3.9.6\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

if not "%JAVA_HOME%" == "" (
    set "JAVACMD=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVACMD=java.exe"
)

"%JAVACMD%" -Dfile.encoding=UTF-8 -jar "%DIR%.mvn\wrapper\maven-wrapper.jar" %*
exit /b %ERRORLEVEL%
