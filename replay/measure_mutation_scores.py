# -*- coding: utf-8 -*-
"""
This script runs a given CSV test suite against all the Scanette mutants (Dadeau+Jumble)

Usage:

    python measure_mutation_scores.py test_suite1.csv ...

It is a 'noisy' app that prints progress messages to standard output.

For each <input>.csv file, the detailed outputs of the individual mutation runs
go into a subdirectory called <input>. 

The combined statistical summary results go into "results.csv" by default.
It is suggested that you graph the "Percent" column.

Created on Mon Apr 27 2020.
License: MIT

With some code taken from ExecuteTraceOpti.py by Yves Ledru (MIT license).

@author: Mark Utting
"""

from pathlib import Path
import sys
import shutil
import os
import time
import re
from typing import Tuple


# %% Global defaults

# default output file (use --out=XXX.csv to override this)
OUTPUT_FILE = "results.csv"

HAND_MUTANTS = 49

# Folder of *.jar files, including jumble etc.
JAR_FOLDER = "scanette_with_jumble_jar"

# Package of the classes to mutate (and their unit tests)
PACKAGE = "fr.ufc.l3info.oprog."

# class path entries (plus either scanette.jar or a mutant .jar)
# Not used at the moment.
otherJarsNames = [
    "ScanetteTestReplay.jar",
    "json-simple.jar",
    "junit-4.12.jar",
    "hamcrest-core-1.3.jar",       # needed by junit
    "jumble_binary_1.3.0.jar",     # creates Jumble mutants (from http://jumble.sourceforge.net)
    "../out/production/scanette",  # Scanette code (or could use scanette.jar?)
    "../out/test/scanette",        # My extra JUnit test: tests/fr/ufc/l3info/oprog/TestCsv.java
    ]

CLASSPATH_SEP = ";" if os.name == "nt" else ":"


# %% Functions copied from Yves' ExecuteTraceOpti.py script.

def retChar(returnCode: int) -> str:
    """Computes a result character corresponding to the Java return code."""
    if returnCode == 0:
        rc = "."
    elif returnCode == 1:
        rc = "F"
    elif returnCode == -1:
        rc = "X"
    else:
        rc = "?"
        print("The Java program return code was not 0, 1 or -1.")
        print("This may reveal a problem while invoking Java.")
        print("Maybe the absolutePath variable should be modified.")
    return rc

def executeCsvFile(csv_file, jar_name, output_dir) -> str:
    """Execute jar_name (typically a mutant) on test case csv_file.

    Returns a letter corresponding to the return code ('F' means test failed = mutant killed.)
    Detailed return messages are stored in resultFile and errorFile
    """
    jars = [jar_name] + [JAR_FOLDER + "/*"]   # WAS: otherJarsNames
    cp = CLASSPATH_SEP.join(jars)
    results = output_dir / f"result_{jar_name}.txt"
    errors = output_dir / f"errorFile_{jar_name}.txt"
    commande = f"java -cp {cp} fr.philae.ScanetteTraceExecutor {csv_file} >{results} 2>{errors}"
    # print(f"Trying: {commande}")
    returnCode = os.system(commande)
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


def run_jumble(csv_file, class_to_mutate, output_dir) -> int:
    """Run Jumble on the given `class_to_mutate` with default mutation operators.

    Full Jumble output is saved in `<output_dir>/result_jumble_<class_to_mutate>.txt`.

    Returns the number of mutants that were detected.
    """
    # The JUnit rerun adapter (TestCsv) requires input to be in "tests.csv".
    shutil.copyfile(csv_file, Path("tests.csv"))
    cp = JAR_FOLDER + "/*"
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
        print(f"  {name}-mutants: ", end="")
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


# %% Measure mutation scores for the given test suites.

def main(args):
    if len(args) >= 2:
        start = 1
        out_file = OUTPUT_FILE
        if args[start].startswith("--out="):
            out_file = args[start].split("=")[1]
            start += 1
        with Path(out_file).with_suffix(".csv").open("w") as out:
            out.write(f"CSV,Events,Hand,MaCaisse,Scanette,Total,Percent\n")
            for csv in args[start:]:
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
                out.write(f"{csv},{events},{hand},{caisse},{scanette},{killed},{percent:.1f}\n")
    else:
        script = sys.argv[0] or "measure_mutation_scores.py"
        print(f"This script takes CSV files containing test suites of Scanette traces,")
        print(f"then runs each test suite against all mutants, including Jumble mutants.")
        print(f"The resulting mutation scores are saved into a .csv file for later analysis.")
        print(f"It is recommended that you graph the 'Percent' column.")
        print()
        print(f"Usage: python {script} [--out=FILE.csv] test_suite.csv ...")

# %%

if __name__ == "__main__":
    main(sys.argv)
