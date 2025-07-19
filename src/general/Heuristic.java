package general;
import papers.Solution;
import java.util.*;

public class Heuristic {
    protected Instance instance;
    protected double runtime_limit;
    protected long start_time;
    protected Stack<Entry> log;
    protected int best;
    
    // we should maintain a "answer" best non-conflicted solution with lowest k

    public class Entry{
        protected int[] coloring;
        protected double time;
        protected double chromatic;

        public Entry (double t, int[] c, double k){
            coloring = c.clone();
            time  = t;
            chromatic = k;
            
        }

        public String toString(){
            return "TimeStamp: " + time + " Chromatic: " + chromatic + " Coloring " + Arrays.toString(coloring); 
        }
    }

    // constructor for the heuristic class
    public Heuristic(Instance instance, double runtime_limit) {
        this.instance = instance;
        this.runtime_limit = runtime_limit;
        this.best = Integer.MAX_VALUE; // Goal of GCP is to minimize the objective function, so best is set to a max value
        this.start_time = System.currentTimeMillis();

        log  = new Stack<Entry>();
    }

    // gets the current run time of the heuristic in seconds
    public double getCurrRunTime() {
        return (System.currentTimeMillis() - start_time) / 1000.0; 
    }

    //Keeps track of every solution logged by metaheuristic
    public void makeEntry(Solution sol){
        log.push(new Entry (getCurrRunTime(),  sol.getColoring(), sol.getK()));
    }
    
    public double getRuntimeLimit() {
        return runtime_limit;
    }

    public Instance getInstance() {
        return instance;
    }
    // This method can be used to report the current state of the heuristic
    // returns true if the heuristic time is less than runtime limit
    public boolean report() {
        return getCurrRunTime() < runtime_limit;
    }
}