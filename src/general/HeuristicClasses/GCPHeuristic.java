package general.HeuristicClasses;
import java.util.*;
import general.*;
import general.SolutionClasses.*;

/*
 * A GCPHeuristic stands for a Graph Coloring Problem Heuristic,
 * indiciating a heuristic that solves for the chromatic number of a graph.
 * A GCPHeuristic maintains a log of all valid solutions found.
 */
public class GCPHeuristic extends Heuristic {
    protected Stack<Entry> log;

    // constructor for the heuristic class
    public GCPHeuristic(Instance instance, double runtime_limit) {
        super(instance, runtime_limit);
        log  = new Stack<Entry>();
    }

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
    public boolean report(Solution solution, int k) {
        if (solution.isValidSolution() && (log.isEmpty() || log.peek().k > k)) {
            log.push(new Entry(solution.getColoring(), getCurrRunTime(), k));
            System.out.println(log.peek());
        } 
        return report();
    }

    public void printLog() {
        System.out.println("Results: ");
        for (Entry entry : log) {
            System.out.println(entry);
        }
    }

}