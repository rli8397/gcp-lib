package general.HeuristicClasses;

import general.*;
import general.SolutionClasses.Solution;

/*
 * A GCPWrapper is a wrapper class for a KCPHeuristic that solves a GCP by 
 * continously calling a KCPHeuristics with decreasing k values. The strategy 
 * for decreasing k values will be given as a string flag to the constructor.
 */
public abstract class GCPWrapper extends GCPHeuristic {
    private KCPHeuristic heuristic;
    private int k;
    private String reduceKStrategy = "random_restart"; // default strategy for reducing k

    // constructor takes flag for k reducing strategy
    public GCPWrapper(Options options) {
        super(options);
        if (options.extras.containsKey("reduce_k_strategy")) {
            reduceKStrategy = options.extras.get("reduce_k_strategy");
        }
        this.k = options.instance.getMaxChromatic();
        if (reduceKStrategy != "random_restart"
        // && other strategies when implemented in the future
        ) {
            throw new RuntimeException("Unknown k reducing strategy: " + reduceKStrategy);
        }
    }

    public void run() {
        // initializing coloring as null allows KCP heuristic to handle it on its own
        this.heuristic = createKCPHeuristic(this, this.k, null);
        do {
            try {
                heuristic.run();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to instantiate heuristic: " + e.getMessage());
            }

        } while (report() && k > 1);
    }

    // must handle a possible null coloring
    protected abstract KCPHeuristic createKCPHeuristic(GCPHeuristic gcpHeuristic, int k, int[] coloring);

    public boolean report(Solution solution) {
        if (solution == null) {
            throw new RuntimeException("Cannot report a null solution");
        }

        // Verify the k of the solution
        this.k = solution.calcK();

        boolean res = super.report(solution);

        if (verbosity == 3) {
            System.out.println("GCPWRAPPER REPORTING");
            System.out.println("RES: " + res + " SOL VALID: " + solution.isValidSolution());
        }

        if (res && solution.isValidSolution()) {
            // reduce k strategy
            // check k before reducing it, solution may have reduced k unintentionally
            if (verbosity == 3) {
                System.out.println("[DEBUG] Valid solution found with k = " + solution.getK()
                        + ", preparing to reduce k");
            }

            this.k--;

            switch (reduceKStrategy) {
                case "random_restart":
                    this.heuristic = createKCPHeuristic(this, this.k, randomRestart(this.k));
                    break;
                default:
                    throw new RuntimeException("Unknown k reducing strategy: " + reduceKStrategy);
            }

        } else {
            // what should we do if kcp heuristic ends and no valid solution is found?
            this.heuristic = createKCPHeuristic(this, this.k, randomRestart(this.k));
        }

        return res;
    }

    public int[] randomRestart(int k) {
        return Solution.randomColoring(instance, k);
    }

    protected int parseIntOption(String key, int defaultValue) {
        if (cmdline_params.containsKey(key)) {
            try {
                return Integer.parseInt(cmdline_params.get(key).trim());
            } catch (Exception e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    protected double parseDoubleOption(String key, double defaultValue) {
        if (cmdline_params.containsKey(key)) {
            try {
                return Double.parseDouble(cmdline_params.get(key).trim());
            } catch (Exception e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    protected String parseStringOption(String key, String defaultValue) {
        if (cmdline_params.containsKey(key)) {
            return cmdline_params.get(key).toLowerCase();
        }

        return defaultValue;
    }

}
