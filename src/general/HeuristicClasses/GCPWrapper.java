package general.HeuristicClasses;

import general.*;
import general.SolutionClasses.Solution;

/*
 * A GCPWrapper is a wrapper class for a KCPHeuristic that solves a GCP by 
 * continously calling a KCPHeuristics with decreasing k values. The strategy 
 * for decreasing k values will be given as a string flag to the constructor.
 */
public abstract class GCPWrapper<T extends KCPHeuristic<?>> extends GCPHeuristic {
    private KCPHeuristic<?> heuristic;
    private int k;
    private int[] coloring;
    private String reduceKStrategy;

    // constructor takes flag for k reducing strategy
    public GCPWrapper(Instance instance, double runtime, int[] initialColoring, String reduceKStrategy) {
        super(instance, runtime);
        this.k = instance.getMaxChromatic();
        this.reduceKStrategy = reduceKStrategy;
        this.coloring = initialColoring;
    }

    public void run() {
        do {
            try {
                // creates a new instance of a KCPHeuristic
                this.heuristic = createKCPHeuristic(coloring, k);
                heuristic.run();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to instantiate heuristic: " + e.getMessage());
            }
        } while (report(heuristic.getSolution(), k) && k > 1);
    }

    protected abstract T createKCPHeuristic(int[] coloring, int k);

    public boolean report(Solution solution, int k) {
        boolean res = super.report(solution, k);
        if (res && solution.isValidSolution()) {
            // reduce k strategy
            System.out.println("run");
            this.k--;
            switch (reduceKStrategy) {
                case "random_restart":
                    this.coloring = randomRestart(k);
                    break;
                default:
                    throw new RuntimeException("Unknown k reducing strategy: " + reduceKStrategy);
            }
        }
        return res;
    }

    public int[] randomRestart(int k) {
        return Solution.randomColoring(instance, k);
    }

    public int[] getColoring() {
        return coloring;
    }
}
