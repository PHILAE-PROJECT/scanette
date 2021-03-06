Replay usage traces on the scanner app
=========================================================

This program is written in Java (requires Java >= 8). It aims to replay traces produced by the simulator or generated by the tools on the different impementations of the scanner case study. 

# Replay the tests on ALL mutants of the scanner

> `python measure_mutation_scores.py suite1.csv suite2.csv ...`
> `cat results.csv`


# Replay the tests on the reference implementation

The following Java command has to be executed from the directory in which the "ressources" directory is located. 

Depending on the considered format (CSV or Agilkia's JSON), the command lines are the following: 

> `java -cp ScanetteTestReplay.jar:json-simple.jar:junit-4.12.jar:scanette.jar fr.philae.ScanetteTraceExecutor CSV_file.csv`

or

> `java -cp ScanetteTestReplay.jar:json-simple.jar:junit-4.12.jar:scanette.jar fr.philae.ScanetteTraceExecutor JSON_file.json`

by replacing the last parameter on the line. The program checks the file extension to determine which parser should be called.  

Beware, on Windows environments, the classpath separator is not : but ; the command line are: 

> `java -cp ScanetteTestReplay.jar;json-simple.jar;junit-4.12.jar;scanette.jar fr.philae.ScanetteTraceExecutor CSV_file.csv`

ou 

> `java -cp ScanetteTestReplay.jar;json-simple.jar;junit-4.12.jar;scanette.jar fr.philae.ScanetteTraceExecutor JSON_file.json`

Notice that the replay stops when a scenario fails. As for the traces extracted from the simulator, it is not necessary that sessions are distinguished (they can be intertwined). 

If you which to script the executions'runs and observe the return values of the program:  
- 0 normal termination (all tests pass)
- -1 error termination (but not related to the tests: wrong parameterization, unrecognized operation, etc.)
- 1 non-conformance (some tests fail)


# Replay the tests on a single mutant 

The directory contains some mutant implementations. It is not guaranteed that all can be "killed" by the tests. Each mutant is contained in an JAR file named "scanette-muXYZ.jar".

In order to replay of the traces on a specific mutant (for example scanette-mu42.jar) : 

> `java -cp ScanetteTestReplay.jar:json-simple.jar:junit-4.12.jar:scanette-mu42.jar fr.philae.ScanetteTraceExecutor CSV_file.csv`

under Linux/MacOS and 

> `java -cp ScanetteTestReplay.jar;json-simple.jar;junit-4.12.jar;scanette-mu42.jar fr.philae.ScanetteTraceExecutor CSV_file.csv`

under Windows. 

Just change the name of the JAR in the classpath, the rest of the command is unmodified. 
