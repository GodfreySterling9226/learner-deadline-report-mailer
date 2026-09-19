#!/bin/sh
set -eu
rm -rf build
mkdir -p build/classes build/test-classes
javac -d build/classes $(find src/main/java -name '*.java')
javac -cp build/classes -d build/test-classes $(find src/test/java -name '*.java')
java -ea -cp build/classes:build/test-classes edu.report.service.DeadlineReportServiceTest
