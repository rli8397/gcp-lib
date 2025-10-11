package general.HeuristicClasses;
import general.SolutionClasses.Solution;

public class KCPHeuristic<T extends Solution> {
    protected int k;
    protected GCPWrapper<?> wrapper; 

    // should have a reference to GCP wrapper 
    // when reporting a solution, call gcp heuristic's report
    public KCPHeuristic (GCPWrapper<?> wrapper, int k, Class<T> solutionClass) {
        this.wrapper = wrapper;
        this.k = k;
        
        try {
            // Solution solution = solutionClass.getDeclaredConstructor(GCPWrapper.class, int.class).newInstance(gcpWrapper, k);
            // solution.run(); ??
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate solution: " + e.getMessage());
        }

    }
}
