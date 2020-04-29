#!/usr/bin/env bash

TESTS=tests.csv
SIZE=$(cat "$TESTS" | wc -l)
echo "Running all scanette-mu*.jar mutations using $TESTS with $SIZE steps..."

for M in scanette-mu*.jar
do
    java -cp "$M;ScanetteTestReplay.jar;../lib/*" fr.philae.ScanetteTraceExecutor $TESTS
done >tmp.log 2>&1
echo -n "Mutants detected: "
egrep 'AssertionError|Exception' tmp.log | wc -l
echo -n "Mutants NOT detected: "
grep '^[^A-Za-z]*$' tmp.log | wc -l
echo "Total should be 49.  See tmp.log for details..."

