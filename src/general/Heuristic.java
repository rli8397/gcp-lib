package general;
import java.util.*;

import papers.Solution;

public class Heuristic {
    protected Instance instance;
    protected double runtime_limit;
    protected long start_time;
    protected Stack<Entry> log;
    protected static Random rand;
    protected int k;
    
    public int verbosity;

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

    // constructor for the heuristic class
    public Heuristic(Options options) {
        this.instance = options.instance;
        this.runtime_limit = options.runtime;
        rand = new Random(options.seed);
        this.verbosity = options.verbosity;

        //Start timer 
        this.start_time = System.currentTimeMillis();
        log  = new Stack<Entry>();
    }

    // gets the current run time of the heuristic in seconds
    public double getCurrRunTime() {
        return (System.currentTimeMillis() - start_time) / 1000.0; 
    }
    
    public double getRuntimeLimit() {
        return runtime_limit;
    }

    public Instance getInstance() {
        return instance;
    }
    // This method can be used to report the current state of the heuristic
    // returns true if the heuristic time is less than runtime limit
    public boolean report(Solution solution) {
        if (solution.isValidSolution() && (log.isEmpty() || log.peek().k > solution.getK())) {
            log.push(new Entry(solution.getColoring(), getCurrRunTime(), solution.getK()));
        } 

        return report();
    }
    
    public boolean report() {
        return getCurrRunTime() < runtime_limit;
    }
    // generates a number from 0 - n exclusive of n
    public static int random(int n) {
        return rand.nextInt(n);
    }

    // returns the Random object
    public static Random getRandom() {
        return rand;
    }

    public void printLog() {
        
        System.out.println("Results: ");

        for (Entry entry : log) {
            System.out.println(entry);
        }
    }

}