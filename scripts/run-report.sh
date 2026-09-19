#!/bin/sh
set -eu
: "${INFRAI_API_KEY:?Set INFRAI_API_KEY}"
: "${REPORT_RECIPIENT:?Set REPORT_RECIPIENT}"
mkdir -p build/classes
javac -d build/classes $(find src/main/java -name '*.java')
java -cp build/classes edu.report.ReportRunner
