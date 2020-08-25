package fr.philae;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: Frederic Dadeau
 * Date: 08/03/2020
 * Time: 08:45
 */
public class ScanetteTraceExecutor {


    public static void main(String[] args) throws IOException {

        // args = new String[]{"/Users/fred/recherche/projets/PHILAE.ANR/git/scanette/replay/log_split.json"};

        if (args.length != 1) {
            System.err.println("Wrong number of arguments.\nUsage: [run command] trace.csv or [run command] agilkia_trace.json");
            System.exit(-1);
        }
        File f = new File(args[0]);
        if (!f.exists()) {
            System.err.println("Error: file does not exist");
            System.exit(-1);
        }

        if (f.getName().endsWith(".csv")) {
            readFromCSV(f);
            return;
        }
        if (f.getName().endsWith(".json")) {
            readFromJSON(f);
            return;
        }

        System.err.println("Wrong file extension.\nUsage: [run command] trace.csv or [run command] agilkia_trace.json");
        System.exit(-1);

    }


    /**
     *
     * @param csvFile
     * @throws IOException
     */
    public static void readFromCSV(File csvFile) throws IOException {

        // File reading variables
        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        String line;

        // Re-executor
        ScanetteAdapter adapter = new ScanetteAdapter();

        // debug info
        int l = 0;
        HashSet<String> sessions = new HashSet<String>();

        while ((line = br.readLine()) != null) {
            l++;
            String[] tokens = line.split(",");
            if (tokens.length != 7) {
                System.err.println("Error in CSV file format (line " + l + ").\nExpected: #LineID, #Timestamp, #SessionID, #Object, #Operation, #ArrayOfParameters, #ExpectedResult");
            }
            // display progress info
            if (!sessions.contains(tokens[2])) {
                System.out.print(".");
                sessions.add(tokens[2]);
            }
            // extract data
            String obj = tokens[3].trim();
            String op = tokens[4].trim();
            tokens[5] = tokens[5].trim();
            String[] params = tokens[5].substring(1, tokens[5].length()-1).split(",");
            String res = tokens[6].trim();
            // send to processor
            try {
                adapter.process(l, obj, op, params, res);
            } catch (Exception e) {
                System.err.println(line + " (line " + l + ")");
                e.printStackTrace();
                System.exit(-1);
            }
            catch (AssertionError e) {
                System.out.println("F");
                System.err.println(line + " (line " + l + ")");
                e.printStackTrace();
                System.exit(1);
            }
        }
        System.out.println();
    }


    /**
     * 
     * @param f
     */
    private static void readFromJSON(File f) throws IOException {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        // Re-executor
        ScanetteAdapter adapter = new ScanetteAdapter();

        try {
            FileReader reader = new FileReader(f);

            //Read JSON file
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            // retrieve traces
            JSONArray traces = (JSONArray) obj.get("traces");

            // explore the traces
            for (int i=0; i < traces.size(); i++) {
                JSONArray events = (JSONArray) ((JSONObject) traces.get(i)).get("events");
                System.out.print(".");
                for (int j=0; j < events.size(); j++) {
                    JSONObject event = (JSONObject) events.get(j);
                    String op = event.get("action").toString();
                    String input  = null;
                    if (! event.get("inputs").toString().equals("{}")) {
                        input = ((JSONObject)event.get("inputs")).get("param").toString();
                    }
                    String output = "?";
                    if (! event.get("outputs").toString().equals("{}")) {
                        output = ((JSONObject)event.get("outputs")).get("Status").toString();
                    }
                    String objet = ((JSONObject) event.get("meta_data")).get("object").toString();
                    //System.out.println(objet + "," + op + ", " + input + ", " + output);

                    String[] tInput = (input == null) ? new String[0] : new String[]{ input };

                    // send to processor
                    try {
                        adapter.process(0, objet, op, tInput, output);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                    catch (AssertionError e) {
                        System.out.println("F");
                        System.out.println(objet + "," + op + ", " + input + ", " + output);
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
            System.out.println();

        }
        catch (ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
