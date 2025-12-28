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
    private String reduceKStrategy;

    // constructor takes flag for k reducing strategy
    public GCPWrapper(Options options, String reduceKStrategy) {
        super(options);
        this.k = options.instance.getMaxChromatic();
        if (
            reduceKStrategy != "random_restart"
            // && other strategies
        ) {
            throw new RuntimeException("Unknown k reducing strategy: " + reduceKStrategy);
        }

        this.reduceKStrategy = reduceKStrategy;
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

        // Handles logging
        boolean res = super.report(solution);

        if (res && solution.isValidSolution()) {
            // reduce k strategy
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
            this.heuristic = createKCPHeuristic(this, this.k, null);
        }
        
        return res;
    }

    public int[] randomRestart(int k) {
        return Solution.randomColoring(instance, k);
    }

}
