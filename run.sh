#!/usr/bin/env bash
# ==============================================================================
# SmartCity360 - UrbanTech Solutions Enterprise Management Platform
# 
# Usage:
#   ./run.sh         - Compiles and starts Tomcat web server at http://localhost:8080
#   ./run.sh test    - Runs all 36 automated unit and system tests
#   ./run.sh war     - Packages production WAR file to target/smartcity360.war
# ==============================================================================

set -e
cd "$(dirname "$0")"

if [ "$1" == "test" ]; then
    echo "Running SmartCity360 system test suite..."
    mvn test
elif [ "$1" == "war" ]; then
    echo "Packaging SmartCity360 WAR..."
    mvn clean package -DskipTests
    echo "WAR built at target/smartcity360.war"
else
    echo "=============================================================================="
    echo " Starting SmartCity360 Web Application on Tomcat (http://localhost:8080)"
    echo " Press Ctrl+C to stop."
    echo "=============================================================================="
    mvn compile exec:java
fi
