import general.Heuristic;
import general.Instance;
import general.Options;

import java.util.*;
import papers.*;

import java.io.*;

public class main {

    public static void main(String[] args) {

        Options opts = new Options();

        String heuristicName = null;
            
        //Parsing arguments 
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                case "--heuristic":
                    heuristicName = args[++i];
                    break;

                case "-i":
                case "--instance":
                    opts.instance = new Instance(new File(args[++i]));
                    break;

                //optional arg
                case "-s":
                case "--seed":
                    opts.seed = Long.parseLong(args[++i]);
                    break;

                case "-r":
                case "--runtime":
                    opts.runtime = Double.parseDouble(args[++i]);
                    break;

                case "-v":
                case "--verbosity":
                    opts.verbosity = Integer.parseInt(args[++i]);
                    break;

                default:
                    System.out.println("Unknown option: " + args[i]);
                    return;
            }
        }

        //Makes sure heurstic and instance are correctly loaded in
        if (opts.instance == null || heuristicName == null) {
            System.err.println("Usage: java -cp bin Main -i <filePath> -h <heuristicName> -t runtime [-s seed]  [-sol true/false] [-met true/false]");
            return;
        }

        //Refection of heuristic onto class

        try {
            Class<?> heuristicClass = Class.forName("papers." + heuristicName);
            //
            Heuristic heuristic = (Heuristic) heuristicClass.getConstructor(Options.class).newInstance(opts);
        } catch (Exception e) {
            System.err.println("Error loading heuristic: " + e.getMessage());
            e.printStackTrace();
            return;
        }

    }
}

