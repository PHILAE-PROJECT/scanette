#!/usr/bin/env bash

# NOTE: my Jumble JUnit tests *always* use 'tests.csv' as input.
# To use a different csv file, copy it to 'tests.csv'.
TESTS=tests.csv
SIZE=$(cat "$TESTS" | wc -l)
PACKAGE=fr.ufc.l3info.oprog
CP='../lib/*;../out/production/scanette;../out/test/scanette;../implem;../tests'
for SRC in MaCaisse Scanette
do
    echo "Running Jumble on class $SRC using $TESTS with $SIZE steps..."
    java -jar ../lib/jumble_binary_1.3.0.jar -c $CP $PACKAGE.$SRC $PACKAGE.TestCsv
done

