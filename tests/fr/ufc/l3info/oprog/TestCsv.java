package fr.ufc.l3info.oprog;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;

/**
 * This JUnit test is used to support Jumble mutation testing of any Scanette class.
 *
 * It uses an input file called 'tests.csv' in the current directory.
 * (a completely empty file will suffice if you just want all the JUnit tests to pass).
 *
 * It reads the events in that file and sends them to the Scanette (adapter) classes.
 *
 * @author Frederic Dadeau (code copied from ScanetteTraceExecutor).
 * @author Mark Utting
 */
public class TestCsv {

    @Test
    public void testAllWithCsv() throws Exception {
        readFromCSV(new File("tests.csv"));
    }

    // copied from ScanetteTraceExecutor
    public static void readFromCSV(File csvFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        ScanetteAdapter adapter = new ScanetteAdapter();
        int l = 0;
        HashSet sessions = new HashSet();

        String line;
        while((line = br.readLine()) != null) {
            ++l;
            String[] tokens = line.split(",");
            if (tokens.length != 7) {
                System.err.println("Error in CSV file format (line " + l + ").\nExpected: #LineID, #Timestamp, #SessionID, #Object, #Operation, #ArrayOfParameters, #ExpectedResult");
            }

            if (!sessions.contains(tokens[2])) {
                System.out.print(".");
                sessions.add(tokens[2]);
            }

            String obj = tokens[3].trim();
            String op = tokens[4].trim();
            tokens[5] = tokens[5].trim();
            String[] params = tokens[5].substring(1, tokens[5].length() - 1).split(",");
            String res = tokens[6].trim();

            adapter.process(l, obj, op, params, res);
        }

        System.out.println();
    }

}
