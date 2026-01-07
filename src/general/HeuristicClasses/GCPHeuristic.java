package general.HeuristicClasses;
import java.util.*;
import general.*;
import general.SolutionClasses.*;

/*
 * A GCPHeuristic stands for a Graph Coloring Problem Heuristic,
 * indiciating a heuristic that solves for the chromatic number of a graph.
 * A GCPHeuristic maintains a log of all valid solutions found.
 */
public abstract class GCPHeuristic extends Heuristic {
    protected Stack<Entry> log;
    protected int verbosity;

    // constructor for the heuristic class
    public GCPHeuristic(Options options) {
        super(options);
        verbosity = options.verbosity;
        log  = new Stack<Entry>();
    }
    
    public abstract void run();

    // we should maintain a "answer" best non-conflicted solution with lowest k
    public class Entry {
        protected int[] coloring;
        protected double time;
        protected int k;

        public Entry (int[] coloring, double time, int k){
            this.coloring = coloring.clone();
            this.time  = time;
            this.k = k;
        }

        public String toString(){
            return "\nTimeStamp: " + time + "\nColors: " + k + "\nColoring " + Arrays.toString(coloring); 
        }
    }


    // This method can be used to report the current state of the heuristic
    // returns true if the heuristic time is less than runtime limit
    public boolean report(Solution solution) {

        boolean isBetter = solution.isValidSolution() && (log.isEmpty() || log.peek().k > solution.getK());
        log.push(new Entry(solution.getColoring(), getCurrRunTime(), solution.getK()));

         // Level 0: Silent mode
        if (verbosity == 0) {}

         // Level 1: Only print when a new best is found
        if (verbosity == 1) {
            if (isBetter) {
                System.out.println("[BEST] New best solution found: k = " + solution.getK()
                        + " | time = " + getCurrRunTime() + "s");
            }
        }

        // Level 2: Print all valid solutions (even if not improving)
        if (verbosity == 2) {
            //Valid but not better necessarily better
            if (solution.isValidSolution()) {
                System.out.println("[SOLUTION] k = " + solution.getK()
                        + " | time = " + getCurrRunTime() + "s");
            }
        }

         // Level 3: Debug mode — print everything thats reported, and the coloring itself
        if (verbosity >= 3) {
            log.push(new Entry(solution.getColoring(), getCurrRunTime(), solution.getK()));
            System.out.println("[DEBUG] k = " + solution.getK()
                    + " | time = " + getCurrRunTime() + "s"
                    + " | valid = " + solution.isValidSolution());
            System.out.println("Coloring: " + Arrays.toString(solution.getColoring()));
        }

        return report();
    }

    public int[] getColoring() {
        if (log.isEmpty()) {
            return null;
        }
        return log.peek().coloring;
    }

    public void printLog() {
        System.out.println("Results: ");
        for (Entry entry : log) {
            System.out.println(entry);
        }
    }

}