# How to Mutation-Test Scanette with Jumble

1. Rename your test traces to tests.csv
2. Run the jumble.sh script.

This will create about 30 mutations for MaCaisse.java and 26 for Scanette.java
and run your test traces on each one of those mutations.
Note that you need to run it in this directory, as the script relies on finding:
 * all the necessary *.jar files in ../lib
 * all the Scanette *.class files either in ../out/production/scanette (IntelliJ puts them there)
   or in ../implem (if you compile them yourself with javac)
 * all the Scanette JUnit test *.class files either in ../out/test/scanette (IntelliJ)
   or in ../tests (if you compile them yourself with javac)

For details about how Jumble works and its output, see:

    http://jumble.sourceforge.net


