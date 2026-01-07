import general.*; // or the correct base heuristic type you use
import java.util.*;
import java.io.*;

public class main {

    public static void main(String[] args) {
        // ---- defaults & holders ----
        String heuristicName = null;
        Options opts = new Options();
        String paramsFilePath = null;

        // ---- parse command-line args ----
        // Expected usage:
        // java -cp bin main -h <heuristicName> -i <instancePath> -s <seed> -r <runtime> -v <verbosity> [-p <paramsFile>]
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "-h":
                case "--heuristic":
                    if (i + 1 >= args.length) { System.err.println("Missing value for " + a); return; }
                    heuristicName = args[++i];
                    break;

                case "-i":
                case "--instance":
                    if (i + 1 >= args.length) { System.err.println("Missing value for " + a); return; }
                    opts.instanceFile = new File(args[++i]);
                    break;

                case "-s":
                case "--seed":
                    if (i + 1 >= args.length) { System.err.println("Missing value for " + a); return; }
                    try { opts.seed = Long.parseLong(args[++i]); }
                    catch (NumberFormatException ex) { System.err.println("Invalid seed"); return; }
                    break;

                case "-r":
                case "--runtime":
                    if (i + 1 >= args.length) { System.err.println("Missing value for " + a); return; }
                    try { opts.runtime = Double.parseDouble(args[++i]); }
                    catch (NumberFormatException ex) { System.err.println("Invalid runtime"); return; }
                    break;

                case "-v":
                case "--verbosity":
                    if (i + 1 >= args.length) { System.err.println("Missing value for " + a); return; }
                    try { opts.verbosity = Integer.parseInt(args[++i]); }
                    catch (NumberFormatException ex) { System.err.println("Invalid verbosity"); return; }
                    break;

                case "-p":
                case "--params":
                    if (i + 1 >= args.length) { System.err.println("Missing value for " + a); return; }
                    paramsFilePath = args[++i];
                    break;

                default:
                    // allow the last positional arg to be the params file if it looks like a file and no -p used
                    if (i == args.length - 1 && paramsFilePath == null && (a.contains("=") == false && (new File(a)).exists())) {
                        paramsFilePath = a;
                    } else {
                        System.err.println("Unknown option: " + a);
                        System.err.println("Usage: java -cp bin main -h <heuristicName> -i <instancePath> [-s seed] [-r runtime] [-v verbosity] [-p paramsFile]");
                        return;
                    }
            }
        }

        // ---- basic validation of required CLI options ----
        if (heuristicName == null) {
            System.err.println("Missing heuristic. Use -h <heuristicName>");
            return;
        }
        if (opts.instanceFile == null) {
            System.err.println("Missing instance. Use -i <instancePath>");
            return;
        }
        if (!opts.instanceFile.exists()) {
            System.err.println("Instance file not found: " + opts.instanceFile.getAbsolutePath());
            return;
        }

        // populate Instance object now that instance file validated
        try {
            opts.instance = new Instance(opts.instanceFile);
        } catch (Exception e) {
            System.err.println("Error constructing Instance from file: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // ---- parse params file into opts.extras (if provided) ----
        if (paramsFilePath != null) {
            File paramFile = new File(paramsFilePath);
            if (!paramFile.exists()) {
                System.err.println("Parameter file not found: " + paramFile.getAbsolutePath());
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(paramFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    // expect key = value or key=value
                    String[] parts = line.split("=", 2);
                    if (parts.length < 2) continue;

                    String key = parts[0].trim();
                    String value = parts[1].trim();

                //Process known keys or add to extras
                switch (key.toLowerCase()) {
                    case "heuristic":
                        heuristicName = value;
                        break;

                    case "instance":
                        opts.instance = new Instance(new File(value));
                        break;

                    case "seed":
                        opts.seed = Long.parseLong(value);
                        break;

                    case "runtime":
                        opts.runtime = Double.parseDouble(value);
                        break;

                    case "verbosity":
                        opts.verbosity = Integer.parseInt(value);
                        break;

                    // If key is not recognized, add to extras
                    default:
                        opts.extras.put(key.toLowerCase(), value);  
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading parameter file: " + e.getMessage());
            return;
        }

        // ---- debug print of final options ----
        if (opts.verbosity >= 2) {
            System.out.println("Running heuristic: " + heuristicName);
            System.out.println(opts.toString());
        }

        // ---- dynamic heuristic loading (reflection) ----
        try {
            Class<?> heuristicClass = Class.forName("papers." + heuristicName);

            // expect a constructor that accepts Options
            GCPHeuristic heuristic = (GCPHeuristic) heuristicClass
                    .getConstructor(Options.class)
                    .newInstance(opts);
            System.out.println("Heuristic starting");
            heuristic.run();
            System.out.println("Heuristic " + heuristicName + " ran successfully.");

        } catch (NoSuchMethodException e) {
            System.err.println("Heuristic class must have a constructor taking (Options).");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Heuristic class not found: papers." + heuristicName);
        } catch (Exception e) {
            System.err.println("Error loading or running heuristic: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
