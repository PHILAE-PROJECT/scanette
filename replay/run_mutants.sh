#!/usr/bin/env bash

TESTS=tests.csv
SIZE=$(wc $TESTS)
echo "Running all scanette-mu*.jar mutations using $TESTS of size $SIZE"

for M in scanette-mu*.jar
do
    java -cp "ScanetteTestReplay.jar;json-simple.jar;junit-4.12.jar;$M" fr.philae.ScanetteTraceExecutor $TESTS
done >tmp.log 2>&1
echo -n "Mutants detected: "
egrep 'AssertionError|Exception' tmp.log | wc -l
echo -n "Mutants NOT detected: "
grep '^[^A-Za-z]*$' tmp.log | wc -l
echo "Total should be 49.  See tmp.log for details..."

