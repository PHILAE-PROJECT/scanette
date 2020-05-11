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
   or in ../tests (if you compile them yourself with javac)


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

def read_jumble_results(result_file: Path) -> Tuple[int, int]:
    """Reads Jumble output and gets the number of mutants killed and total."""
    output = result_file.read_text()
    mutants_match = re.search("Mutation points = ([0-9]+),", output)
    percent_match = re.search("Score: ([0-9]*)%", output)
    # print("mutants", mutants_match, " percent", percent_match)
    if mutants_match and percent_match:
        # Note: This might be +/-1 if a large class has more than 100 mutants
        # The alternative is to parse all the Jumble output.
        mutants = int(mutants_match.group(1))
        percent = int(percent_match.group(1))
        killed = int( mutants * percent / 100.0 + 0.5 )
        return (killed, int(mutants))
    else:
        print(f"\nWARNING: could not read Jumble results from {result_file}.")
        return (0, 0)


def run_jumble(csv_file: Path, class_to_mutate: str, output_dir: Path) -> int:
    """Run Jumble on the given `class_to_mutate` with default mutation operators.

    Full Jumble output is saved in `<output_dir>/result_jumble_<class_to_mutate>.txt`.

    Returns the number of mutants that were detected.
    """
    # The JUnit rerun adapter (TestCsv) requires input to be in "tests.csv".
    shutil.copyfile(csv_file, Path("tests.csv"))
    cp = CLASSPATH_SEP.join(otherJarsNames)
    results = output_dir / f"result_{class_to_mutate}.txt"
    errors = output_dir / f"errorFile_{class_to_mutate}.txt"
    clazz = PACKAGE + class_to_mutate
    test = PACKAGE + "TestCsv"
    commande = f"java -cp {cp} com.reeltwo.jumble.Jumble {clazz} {test} >{results} 2>{errors}"
    # print(f"Trying: {commande}")
    returnCode = os.system(commande)
    if returnCode == 0 and results.exists():
        return read_jumble_results(results)
    else:
        return (0, 0)   # ignore this class.  Error should have already been printed?


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
        print("".join(summary), end="")
    else:
        print(f"  Jumble {name}: ", end="", flush=True)
        (score, total) = run_jumble(csv_file, name, outdir)
    end = time.perf_counter()
    print(f" score={score}/{total}  [{end-start:.1f} secs]", flush=True)
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

# %%

if __name__ == "__main__":
    main(sys.argv)
