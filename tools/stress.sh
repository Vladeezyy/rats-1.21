#!/bin/sh
# Server load test (common/src/selftest/.../StressTest.java) on a fresh dev server world: mean tick time (MSPT) with an
# empty stage, with RATS vanilla wolves and with RATS tamed rats at work, recorded with JFR. Prints the measured lines
# and the Rats methods the server thread spent its samples in (tools/jfr_hot.py).
# Usage: [LOADER=fabric] [RATS=100] sh tools/stress.sh
cd "$(dirname "$0")/.."
LOADER=${LOADER:-neoforge}
RATS=${RATS:-100}
JFR_DIR=$PWD/build/stress-jfr
rm -rf "$JFR_DIR"; mkdir -p "$JFR_DIR"
# no spaces inside an option: JAVA_TOOL_OPTIONS is split on them (it reaches gradle and the game's JVM; each JVM writes
# its own file into the folder)
JAVA_TOOL_OPTIONS="-Drats.stress=$RATS -XX:FlightRecorderOptions:stackdepth=256 -XX:StartFlightRecording=settings=profile,dumponexit=true,filename=$JFR_DIR/" \
  sh tools/selftest.sh "$@" > build-stress.txt 2>&1
grep -E 'Rats stress\]' build-selftest.log | sed -E 's/^.*\[Rats stress\] //'
grep -E 'TIMEOUT|BUILD FAILED|crash report' build-stress.txt
# the game's recording is the largest one
JFR=$(ls -S "$JFR_DIR"/*.jfr 2>/dev/null | head -1)
# the rat phase is the last 70 s of the recording
[ -n "$JFR" ] && python3 tools/jfr_hot.py "$JFR" 15 70
