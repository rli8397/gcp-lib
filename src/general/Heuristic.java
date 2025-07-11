package general;
public class Heuristic {
    protected Instance instance;
    protected double runtime_limit;
    protected long start_time;
    protected int best;

    // constructor for the heuristic class
    public Heuristic(Instance instance, double runtime_limit) {
        this.instance = instance;
        this.runtime_limit = runtime_limit;
        this.best = Integer.MAX_VALUE; // Goal of GCP is to minimize the objective function, so best is set to a max value
        this.start_time = System.currentTimeMillis();
    }

    // gets the current run time of the heuristic in seconds
    public double getCurrRunTime() {
        return (System.currentTimeMillis() - start_time) / 1000.0; 
    }

    public void startHeuristic() {
        start_time = System.currentTimeMillis();
    }
    
    // This method can be used to report the current state of the heuristic
    // returns true if the heuristic time is less than runtime limit
    public boolean report() {
        return getCurrRunTime() < runtime_limit;
    }
}