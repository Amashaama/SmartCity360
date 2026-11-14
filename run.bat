@echo off
rem ==============================================================================
rem SmartCity360 - UrbanTech Solutions Enterprise Management Platform
rem 
rem Usage:
rem   run.bat         - Compiles and starts Tomcat web server at http://localhost:8080
rem   run.bat test    - Runs all 36 automated unit and system tests
rem   run.bat war     - Packages production WAR file to target/smartcity360.war
rem ==============================================================================

cd /d "%~dp0"

if /i "%1"=="test" (
    echo Running SmartCity360 system test suite...
    mvn test
) else if /i "%1"=="war" (
    echo Packaging SmartCity360 WAR...
    mvn clean package -DskipTests
    echo WAR built at target/smartcity360.war
) else (
    echo ==============================================================================
    echo  Starting SmartCity360 Web Application on Tomcat (http://localhost:8080)
    echo  Press Ctrl+C to stop.
    echo ==============================================================================
    mvn compile exec:java
)
