#!/bin/sh
# Starts the Dental Clinic application (needs Java 17+ and Maven in the PATH).
cd "$(dirname "$0")" && mvn -q javafx:run
