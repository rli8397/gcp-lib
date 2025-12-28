package general.HeuristicClasses;
import general.SolutionClasses.Solution;
import general.*;
/*
 * A KCPHeuristic stands for a k-Coloring Problem Heuristic, meaning
 * it solves to see if a graph can be colored with k colors. 
 * This is different from GCPHeuristic because once it finds a valid k-coloring
 * it stops. Instead of a log of solutions, it maintains a single solution, which is
 * either a valid k-coloring (if found) or the best coloring found so far (minimizes objective function)
 */
public abstract class KCPHeuristic extends Heuristic {
    protected int k;
    protected Solution solution;
    protected GCPHeuristic gcp = null;

    public KCPHeuristic (Options options, int k) {
        super(options);
        this.k = k; 
    }

    public KCPHeuristic (GCPHeuristic gcp, int k) {
        super(gcp);
        this.gcp = gcp;
        this.k = k;
    }

    public abstract void run();
    
    public int getK() {
        return k;
    }

}
