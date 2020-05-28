# -*- coding: utf-8 -*-
"""
This script runs a given CSV test suite against all the Scanette mutants (Dadeau+Jumble)

Usage:

    python measure_mutation_scores.py test_suite1.csv ...

The combined statistical summary results go into "results.csv" by default.
It also generates a graph of the total "Percent" column into "results.png".

It is a 'noisy' app that prints progress messages to standard output.

For each <input>.csv file, the detailed outputs of the individual mutation runs
go into a subdirectory called <input>. 

NOTE: you need to run it in this directory, as the script relies on finding:
 * all the necessary *.jar files in ../lib
 * all the Scanette *.class files either in ../out/production/scanette (IntelliJ puts them there
   when you build the project) or in ../implem (if you compile them yourself with javac)
 * all the Scanette JUnit test *.class files either in ../out/test/scanette (IntelliJ)
   or in ../tests, if you compile them yourself with javac, for example, as follows:
                   cd implem; javac --release 8 fr/ufc/l3info/oprog/*.java
                   cd tests; javac --release 8 -cp "../implem;../lib/*" fr/ufc/l3info/oprog/*.java
    NOTE: if you are using Java 8 you can omit the --release 8)


Created on Mon Apr 27 2020.
License: MIT

With some code taken from ExecuteTraceOpti.py by Yves Ledru (MIT license).

@author: Mark Utting
"""

from pathlib import Path
import sys
import glob
import shutil
import os
import subprocess
import time
import re
from typing import Tuple, List
import pandas as pd
import matplotlib.pyplot as plt


# %% Global defaults

# default output file (use --out=XXX.csv to override this)
OUTPUT_FILE = "results.csv"

HAND_MUTANTS = 49

# Package of the classes to mutate (and their unit tests)
PACKAGE = "fr.ufc.l3info.oprog."

# class path entries (plus either scanette.jar or a mutant .jar)
otherJarsNames = [
    "../lib/*",     # all necessary *.jar files, including jumble and ScanetteTestReplay.jar
    "../out/production/scanette",  # Scanette .class files, compiled from ../implem
    "../out/test/scanette",        # JUnit tests (*.class), compiled from ../tests
    "../implem",  # .class files for non-IntelliJ users
    "../tests",   # .class files for non-IntelliJ users
    ]

CLASSPATH_SEP = ";" if os.name == "nt" else ":"


# %% Functions copied from Yves' ExecuteTraceOpti.py script.

def retChar(returnCode: int) -> str:
    """Computes a result character corresponding to the Java return code."""
    if returnCode == 0:
        rc = "."
    elif returnCode == 1:
        rc = "F"
    elif returnCode in [-1, 255, 4294967295]:  # -1 as int, uint8, or uint32.
        rc = "X"
    else:
        rc = "?"
        print(f"The Java program return code was not 0, 1 or -1, but was {returnCode}")
        print("This may reveal a problem while invoking Java.")
        print("Maybe the class path should be modified.")
    return rc

def executeCsvFile(csv_file: Path, jar_name: str, output_dir: Path) -> str:
    """Execute jar_name (typically a mutant) on test case csv_file.

    Returns a letter corresponding to the return code ('F' means test failed = mutant killed.)
    Detailed return messages are stored in resultFile and errorFile
    """
    jars = [jar_name] + otherJarsNames
    cp = CLASSPATH_SEP.join(jars)
    args = ["java", "-cp", cp, "fr.philae.ScanetteTraceExecutor", str(csv_file)]
    with open(output_dir / f"result_{jar_name}.txt", "w") as results:
        with open(output_dir / f"errorFile_{jar_name}.txt", "w")as errors:
            proc = subprocess.Popen(args, stderr=errors, stdout=results)
            proc.communicate()
            returnCode = proc.returncode
            return retChar(returnCode)


# %%

def parse_jumble_results(output: str, name: str) -> Tuple[int, int, str, List[str]]:
    """Reads Jumble output and gets the number of mutants killed etc.

    Returns a tuple (killed, mutants, result_string, error_list).

    For example: (2, 7, "...M.T.", ["M Fail: pkg.Hello:130: - -> +"]).

    Note that in the results string:
        - '.' means that the mutant was killed (which is good).
        - 'M' means the mutant was NOT detected (which is bad - inadequate tests)
        - 'T' means timeout (which we usually treat the same as killed, ie. good)
    For each 'M' failure, there will be a corresponding message in error_list.
    """
    mutants = 0
    killed = 0
    results = ""
    errors = []
    collecting_results = False
    for line in output.split("\n"):
        if line.startswith("Mutation points = "):
            mutants = int(line.split(",")[0].split()[-1])
            collecting_results = True
            # print(f"start collecting with mutpoints={mutants}")
        elif collecting_results:
            alldots = re.search("^[T\.]*$", line)
            killed = re.search("^[T\.]*M", line)
            if alldots is not None:
                results += alldots.group(0)
                # print(f"results += '{alldots.group(0)}'")
            elif killed is not None:
                s = killed.group(0)
                results += s
                errmsg = line[len(s) - 1 : ]
                # print(f"results += '{s}' with errmsg={errmsg}")
                errors.append(errmsg)
            else:
                collecting_results = False
                # print("stop collecting")
        elif line.startswith("Score: "):
            percent = int(line.split()[-1][:-1])  # take last word and drop the final '%'
            # Note: This might be +/-1 if a large class has more than 100 mutants
            killed1 = int( mutants * percent / 100.0 + 0.5 )
            killed = len([c for c in results if c == '.'])
            if killed != killed1:
                print(f"Note: killed% rounding error corrected: {killed1} -> {killed}")
            # print(f"percent={percent} so killed={killed} / {len(results)} results: {results}")
        else:
            pass
            # print("ignoring:", line)
    #mutants_match = re.search("Mutation points = ([0-9]+),", output)
    #percent_match = re.search("Score: ([0-9]*)%", output)
    # print("mutants", mutants_match, " percent", percent_match)
    if mutants > 0 and len(results) == mutants:
        return (killed, mutants, results, errors)
    else:
        err = f"Error: could not parse Jumble results from {name}, {killed}/{mutants} {results}"
        print(err)
        return (0, 0, "", [err])


def test_parse_jumble_results():
    eg = """.......
Mutating fr.Chaire
Tests: fr.TestChaire
Mutation points = 22, unit test time limit 2.22s
....M FAIL: fr.Chair:56: & -> |
........M FAIL: fr.Chaire:124: negated conditional
....T..
M FAIL: fr.ufc.l3info.oprog.MaCaisse:174: - -> +

Jumbling took 27.349s
Score: 82%

    """
    (k, m, r, errs) = parse_jumble_results(eg, "test")
    assert k == 18
    assert m == 22
    assert r == "....M........M....T..M"
    assert len(errs) == 3
    assert errs[0] == "M FAIL: fr.Chair:56: & -> |"


def run_jumble(csv_file, class_to_mutate, output_dir) -> Tuple[int, int, str, List[str]]:
    """Run Jumble on the given `class_to_mutate` with default mutation operators.

    Full Jumble output is saved in `<output_dir>/result_jumble_<class_to_mutate>.txt`.

    Returns the number of mutants that were detected.
    """
    # The JUnit rerun adapter (TestCsv) requires input to be in "tests.csv".
    shutil.copyfile(csv_file, Path("tests.csv"))
    cp = CLASSPATH_SEP.join(otherJarsNames)
    results_path = output_dir / f"result_{class_to_mutate}.txt"
    clazz = PACKAGE + class_to_mutate
    test = PACKAGE + "TestCsv"
    args = ["java", "-cp", cp, "com.reeltwo.jumble.Jumble", clazz,test]
    with open(results_path, "w") as results:
        with open(output_dir / f"errorFile_{class_to_mutate}.txt", "w")as errors:
            proc = subprocess.Popen(args, stderr=errors, stdout=results)
            proc.communicate()
            returnCode = proc.returncode
            if returnCode == 0 and results_path.exists():
                return parse_jumble_results(results_path.read_text(), str(results_path))
            else:
                return (0, 0, "", [])   # ignore this class.  Error should have been printed?


# %%

def output_directory(csv_file: Path) -> Path:
    """Determines (and creates if necessary) the directory where detailed output is saved."""
    outdir = csv_file.with_suffix("")  # remove suffix
    if not outdir.exists():
        outdir.mkdir()
    return outdir


def run_mutants(name: str, csv_file: Path, outdir: Path) -> int:
    """
    Measure the various mutation scores with the given CSV test suite.

    Parameters
    ----------
    name : str
        Must be one of the keys in the `mutations` dictionary..
    csv_file : Path
        The input file of traces to use as test data.
    outdir : Path
        Directory to save detailed results into.

    Returns
    -------
    int
        Number of mutants killed.
    """
    start = time.perf_counter()
    if name == "Hand":
        print(f"  {name}-mutants: ", end="", flush=True)
        score = 0
        total = HAND_MUTANTS
        summary = []
        for m in range(1, total + 1):
            mutant = f"scanette-mu{m}.jar"
            result = executeCsvFile(csv_file, mutant, outdir)
            summary.append(result)
            if result in ["F", "X"]:
                score += 1
        summary_str = "".join(summary)
    else:
        print(f"  Jumble {name}: ", end="", flush=True)
        (score, total, summary_str, _) = run_jumble(csv_file, name, outdir)
    end = time.perf_counter()
    print(f" score={score}/{total}  [{end-start:.1f} secs] {summary_str}", flush=True)
    return (score, total)


# %%

def run_experiments(csv_files: List[str]) -> pd.DataFrame:
    """Run one experiment for each CSV file, measuring all its mutation coverage.

    Returns the results as a Pandas DataFrame (one row per experiment).
    """
    rows = []
    for csv in csv_files:
        csv_file = Path(csv)
        outdir = output_directory(csv_file)
        print(f"Testing {csv_file} -- full output is in {outdir}/*", flush=True)
        events = len([line for line in csv_file.read_text().split("\n") if line])
        (hand, hand_max) = run_mutants("Hand", csv_file, outdir)
        (caisse, caisse_max) = run_mutants("MaCaisse", csv_file, outdir)
        (scanette, scanette_max) = run_mutants("Scanette", csv_file, outdir)
        killed = hand + caisse + scanette
        total = hand_max + caisse_max + scanette_max
        percent = 100.0 * killed / total
        rows.append({"CSV": csv, "Events": events, "Hand": hand, "MaCaisse": caisse,
                     "Scanette": scanette, "Total": total, "Percent": percent})
    return pd.DataFrame(rows)


# %% Measure mutation scores for the given test suites.

def main(args):
    start = 1
    out_file = OUTPUT_FILE
    if start < len(args) and args[start].startswith("--out="):
        out_file = args[start].split("=")[1]
        start += 1
    csv_files = args[start:]
    if len(csv_files) > 0:
        if len(csv_files) == 1 and "*" in csv_files[0]:
            # expand this pattern, because Windows does not expand them by default!
            csv_files = glob.glob(csv_files[0])
        data = run_experiments(csv_files)
        data.Percent = data.Percent.round(2)
        data.to_csv(Path(out_file).with_suffix(".csv"), index_label="Index")
        # just for fun we also plot the hand and jumble percentages
        data["ByHand"] = 100.0 * data.Hand / HAND_MUTANTS
        data["Jumble"] = 100.0 * (data.MaCaisse + data.Scanette) / (30 + 26)
        data["Total"] = data.Percent
        data.plot.line(x="CSV", y=["Total", "ByHand", "Jumble"], ylim=(0,100))
        plt.savefig(Path(out_file).with_suffix(".png"))
        # plt.show()
    else:
        script = sys.argv[0] or "measure_mutation_scores.py"
        print(f"This script takes CSV files containing test suites of Scanette traces,")
        print(f"then runs each test suite against all mutants, including Jumble mutants.")
        print(f"The resulting mutation scores are saved into a *.csv file for later analysis.")
        print(f"It also graphs the mutation scores into an output *.png graph.")
        print(f"The default output file names are {OUTPUT_FILE}/.png.")
        print(f"NOTE that you need all the *.java files in ../implem and ../tests compiled")
        print(f"into *.class files.  See instructions at the top of this script for details.")
        print()
        print(f"Usage: python {script} [--out=FILE.csv] test50.csv test100.csv test150.csv ...")


# %% TODO: add grouping if multiple experiments are for same size?

def get_size(s: str) -> int:
    """Get the size NNN from a string like 'name_NNN_etc.csv'. """
    return int(s.split('_')[1])

# datau['Size'] = datau.CSV.apply(get_size)
# grp = datau.groupby('Size')
# grp.Percent.mean().plot.line(yerr=grp.Percent.std(), ylim=(0,100))

# %%

if __name__ == "__main__":
    test_parse_jumble_results()
    main(sys.argv)
