#!/usr/bin/env bash

# NOTE: my Jumble JUnit tests *always* use 'tests.csv' as input.
# To use a different csv file, copy it to 'tests.csv'.
TESTS=tests.csv
SIZE=$(cat "$TESTS" | wc -l)

CP='json-simple.jar;junit-4.12.jar;../out/production/scanette;../out/test/scanette' 
for SRC in fr.philae.femto.{MaCaisse,Scanette}
do
    echo "Running Jumble on $SRC using $TESTS with $SIZE steps..."
    java -jar jumble_binary_1.3.0.jar -c $CP $SRC TestCsv
done

