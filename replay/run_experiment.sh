#!/usr/bin/env bash

#SEED=""  # if you want each run to be randomly different
SEED="--seed=1234"

REPEAT="--repeat=10"

# For optional CLUSTERING stage:
# Path to the read_scanette_csv.py script (from Agilkia repository: examples/scanner)
# or copy that script into this directory
# or use scanettetools/csv2Agilkia/csvToAgilkia.py and adjust the output file name.
# or (RECOMMENDED) run this script with your pre-clustered *.json file!
READ_SCANETTE="read_scanette_csv.py"

if [ -r "$1" ]; then
	input=$(basename "$1")
	case "$input" in
    *.json)
	  out=$(basename "$input" ".json")
	  echo "# Creating output folder: $out"
	  mkdir "$out" || exit 1
	  json="$out/$input"
	  cp "$1" "$json"
    ;;
    *.csv)
	  out=$(basename "$input" ".csv")
	  echo "# Creating output folder: $out"
	  mkdir "$out" || exit 1
	  cp "$1" "$out/$out.csv"
	  echo "# Using Agilkia/examples/scanner/read_scanette_csv.py to do MeanShift clustering"
	  # NOTE: if you do not --cluster, then output json file will be $out/$out.split.json"
	  echo python "$READ_SCANETTE" --split --cluster "$out/$out.csv"
	  json="$out/$out.clustered.json"
    ;;
    *)
      echo "ERROR: unknown file type."
      exit 2
    ;;
    esac
	echo python make_cluster_experiments.py --scanette $REPEAT $SEED "$json"
	echo python measure_mutation_scores.py "$out/testsuite*.csv"
else
	echo "This takes a (clustered) Agilkia *.json file as input,"
	echo "generates many test suites (size=50, 100, ... 350 steps)"
	echo "and measures the mutation score of each test suite."
	echo ""
	echo "Usage: $0 input.json   (a clustered Agilkia traceset)"
fi
