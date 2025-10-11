package general.HeuristicClasses;

import general.*;
import general.SolutionClasses.Solution;

public class GCPWrapper<T extends KCPHeuristic<?>> extends GCPHeuristic {
    private KCPHeuristic<?> heuristic;
    private int k;
    private int[] coloring; 

    // constructor takes flag for k reducing strategy
    public GCPWrapper(Instance instance, double runtime, Class<T> heuristicClass, int[] initialColoring) {
        super(instance, runtime);
        this.k = instance.getMaxChromatic();
        this.coloring = initialColoring;
        while (true) {
            try {
                // creates a new instance of a KCPHeuristic
                this.heuristic = heuristicClass.getDeclaredConstructor( GCPWrapper.class, int.class)
                        .newInstance(this, k);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate heuristic: " + e.getMessage());
            }
        }
    }

    public boolean report(Solution solution, int k) {
        boolean res = super.report(solution, k);
        if (res) {
            // reduce k strat
            this.coloring = randomRestart(k);
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
