# -*- coding: utf-8 -*-
"""
This script generates different sized CSV test suites from a set of clustered traces.

It requires Python 3.7 or higher, and Agilkia 0.6 or higher (to support clustered traces).
E.g.
    conda install -c mark.utting agilkia        # latest version
    conda install -c mark.utting agilkia=0.6

Run it with no command line arguments to see the help message.

    python make_cluster_experiments.py


Created on Mon May 11 2020.
License: MIT

@author: Mark Utting
"""

from pathlib import Path
import math
# from datetime import datetime
import sys
from random import Random
from typing import Iterator
import agilkia


# %% Global defaults

# base name of all the output *.csv/*.json files (in same directory as input file)
OUTPUT = "testsuite"

# %% Scanette CSV-writing functions from agilkia/examples/scanner/write_scanette.py

def scanette_status(intval, floatval):
    """Convert Event status value into Scanette status/result field.

    Scanette result values are sometimes int, sometimes float, sometimes "?".
    """
    if intval == floatval:
        return intval
    if math.isnan(floatval):
        return "?"
    return floatval


def write_scanette_csv(traces: agilkia.TraceSet, path: Path):
    """Writes the given traces into a CSV file in Scanette format.

    Note that the sequence id number and datetime stamp are recorded as meta data of each event,
    with the datatime stamp converted to a Python datetime object.

    The "sessionID", and "object" instance name are recorded as inputs of the event.
    The optional parameter is also added to the inputs under the name "param", if present.

    For example, these two lines might be typical CSV output::

        203, 1584454658227, client9, caisse1, fermerSession, [], 0
        208, 1584454658243, client9, caisse1, payer, [260], 8.67
    """
    # print("now=", datetime.now().timestamp(), datetime.now().isoformat())
    time = 1589179183282  # 11 May 2020.
    with path.open("w") as output:
        n = 0
        for tr in traces:
            for ev in tr:
                n += 1
                if "timestamp" in ev.meta_data:
                    time = int(ev.meta_data["timestamp"].timestamp() * 1000)
                if "param" in ev.inputs:
                    params = f"[{ev.inputs['param']}]"
                else:
                    params = "[]"
                sess = ev.inputs["sessionID"]
                obj = ev.inputs["object"]
                status = scanette_status(ev.status, ev.status_float)
                output.write(f"{n}, {time}, {sess}, {obj}, {ev.action}, {params}, {status}\n")


# %%

def debug(msg):
    # print("   ", msg)
    pass


def generate_suite(traces: agilkia.TraceSet, max_steps: int, output: Path,
                   rand: Random) -> agilkia.TraceSet:
    """
    Generate and return one test suite, containing up to `max_steps` steps.
    """
    result = agilkia.TraceSet([], meta_data=traces.meta_data)
    num_clusters = max(traces.cluster_labels) + 1
    clusters = [traces.get_cluster(i) for i in range(num_clusters)]
    counts = [0 for c in clusters]
    steps = 0
    adding = True
    while adding:
        # terminate if we go through all clusters without adding any traces.
        # (We might still have missed some short traces that could be added, but that seems fair!)
        adding = False
        for i in range(num_clusters):
            # choose random trace from clusters[i], without replacement
            cluster = clusters[i]
            if len(cluster) == 0:
                pass  # debug(f"  skipping empty cluster {i}")
            else:
                chosen = rand.randrange(len(cluster))
                tr = cluster[chosen]
                if steps + len(tr) <= max_steps:
                    cluster.pop(chosen)
                    result.append(tr)
                    steps += len(tr)
                    counts[i] += 1
                    debug(f"{len(result)},{steps}: add trace {chosen} from cluster {i} leaving {len(cluster)} traces")
                    adding = True
                else:
                    debug(f"      skip trace {chosen} len={len(tr)} from cluster {i}")
    countstr = " ".join([str(c) for c in counts])
    print(f"{output} has {steps:3d} steps {len(result):2d} traces with cluster counts {countstr}")
    return result


def generate_suites(traceset: agilkia.TraceSet, sizes: Iterator[int], num_repeats: int,
                    rand: Random, outdir: Path, scanette=False) -> None:
    """
    Generates output test suites of various sizes by selecting from each cluster.

    Parameters
    ----------
    traceset : agilkia.TraceSet
        An Agilkia TraceSet that is already clustered.
    sizes : Iterator[int]
        The sequences of test suite sizes to generate.
    num_repeats : int
        How many random versions of each size to generate.
    rand: random.Random
        The random number generator used for choosing traces from each cluster.
    outdir: Path
        The directory to put the output files into.
    scanette: bool
        If True then output to Scanette *.csv, else output to (Agilkia format) *.json.

    Returns
    -------
    None.
    """
    extn = "csv" if scanette else "json"
    for size in sizes:
        print("")
        for i in range(num_repeats):
            output = outdir / f"{OUTPUT}_{size:03d}_{i:02d}.{extn}"
            # print(f"generating {output}...")
            suite = generate_suite(traceset, size, output, rand)
            # do a final shuffle to increase entropy and avoid ordering biases.
            rand.shuffle(suite.traces)
            if scanette:
                write_scanette_csv(suite, output)
            else:
                suite.save_to_json(output)


# %% Measure mutation scores for the given test suites.

def main(args):
    start = 1
    scanette = False
    num_repeats = 10
    seed = None  # random seed based on wallclock time
    if start < len(args) and args[start] == "--scanette":
        scanette = True
        start += 1
    if start < len(args) and args[start].startswith("--repeat="):
        num_repeats = int(args[start].split("=")[1])
        start += 1
    if start < len(args) and args[start].startswith("--seed="):
        seed = int(args[start].split("=")[1])
        start += 1
    input_files = args[start:]
    if len(input_files) == 1:
        input = Path(input_files[0])
        traceset = agilkia.TraceSet.load_from_json(input)
        rand = Random(seed)
        suites = range(50, 351, 50)
        if traceset.is_clustered():
            nclusters = max(traceset.get_clusters()) + 1
            sizes = [len(traceset.get_cluster(i)) for i in range(nclusters)]
            sizestr = " ".join([str(c) for c in sizes])
            print(f"read {input}: {len(traceset)} traces with {nclusters} clusters: {sizestr}")
        else:
            # not clustered, so we treat whole suite as one big cluster.
            traceset.set_clusters([0 for i in traceset])
            print(f"WARNING: {input} is not clustered, so treating all traces as one cluster.")
            print(f"This can be useful as a null-hypothesis experiment: 'clusters do not help'!")
        generate_suites(traceset, suites, num_repeats, rand, input.parent, scanette)
    else:
        script = sys.argv[0] or "make_cluster_experiments.py"
        print(f"""
This script generates different sized CSV test suites from a set of clustered Scanette traces.

The input file must be in Agilkia JSON format with the traces already clustered.

The output is many test suites (./testsuite_<SIZE>_<NUM>.csv) where:

    SIZE = 50, 100, 150, 200, 250, 300, 350 steps (maximum)
    NUM =  00, 01, 02, 03, ... NUM-1 different random versions of that size.

The test suite generation philosophy is that:
  1. it is (roughly) equally important to test all clusters,
     but larger clusters are slightly more important;
  2. we repeatedly iterate through all the clusters, from largest to smallest;
  3. we select a random trace from each cluster, without replacement.
     (This means that small clusters may become empty, so will be skipped in future iterations) 
  4. all the traces in each generated suite are output into a single CSV file,
     to simulate the fact that we typically have no hard 'reset' operation.
  5. the order of traces in each output file is randomly permuted before output to maximise
     entropy, and thus minimise the chance of results being biased by the order of tests.

For Scanette, it is recommended that the generate output test suites should be post-processed
using the `UniqueObjects.py` Script to reduce interactions between sessions in the same suite:
https://gricad-gitlab.univ-grenoble-alpes.fr/philae/tools/scanettetools/-/tree/master/csvTools

Usage:

    python {script} [--scanette] [--repeat=10] [--seed=None] input_traceset.json

    where:
        --scanette means write output in Scanette *.csv format, rather than Agilkia *.json.
        --repeat=NN means generate NN random suites for each size.
        --seed=NNN means use NNN as the Random seed, to get reproducible results.
""")

# %%

if __name__ == "__main__":
    main(sys.argv)
