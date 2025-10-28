import general.Instance;
import general.Options;
import general.HeuristicClasses.Heuristic;

import java.util.*;
import papers.*;
import general.SolutionClasses.*;
import java.io.*;


public class main {
    public static void main(String[] args) {

        File paramFile = new File(args[0]);
        
        if (!paramFile.exists()) {
            System.err.println("Parameter file not found: " + paramFile.getAbsolutePath());
            return;
        }
        
        //Params + Extended
        Options opts = new Options();
        String heuristicName = null;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(paramFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // skip comments and blank lines
                if (line.isEmpty() || line.startsWith("#")) continue;

                // expected format: key = value
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
                        opts.extras.add(value);  // ASSUME THAT THE EXTENDED PARAMETERS ARE IN THE SAME ORDER
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading parameter file: " + e.getMessage());
            return;
        }

        //Makes sure heurstic and instance are correctly loaded in
        if (opts.instance == null || heuristicName == null) {
            System.err.println("Missing instance and/or heuristic");
            return;
        }

        try {
            Class<?> heuristicClass = Class.forName("papers." + heuristicName);

            // Try constructor with Options and extras list
            Heuristic heuristic = (Heuristic) heuristicClass
                    .getConstructor(Options.class)
                    .newInstance(opts);

            // Run heuristic or do whatever your framework does here
            System.out.println("Heuristic " + heuristicName + " initialized successfully.");

        } catch (NoSuchMethodException e) {
            System.err.println("Heuristic class must have a constructor taking (Options).");
        } catch (Exception e) {
            System.err.println("Error loading heuristic: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

