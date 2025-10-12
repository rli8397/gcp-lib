package general.HeuristicClasses;

import java.util.Random;

import general.Instance;

/*
 * This Heuristic class determines when to start and stop a heuristic
 * based on a runtime limit, this is a functionality all heuristics should have.
 * It also stores the instance being solved and contains a shared random 
 * object to across all classes.
 * 
 * Heuristic is extended by GCPHeuristic and KCPHeuristic.
 */

public class Heuristic {
    protected Instance instance;
    protected double runtime_limit;
    protected long start_time;
    protected static Random rand = new Random(1);

    public Heuristic(Instance instance, double runtime_limit) {
        this.instance = instance;
        this.runtime_limit = runtime_limit;
        this.start_time = System.currentTimeMillis();
    }

    public Instance getInstance() {
        return instance;
    }

    public double getCurrRunTime() {
        return (System.currentTimeMillis() - start_time) / 1000.0;
    }

    public double getRuntimeLimit() {
        return runtime_limit;
    }

    public boolean report() {
        return getCurrRunTime() < runtime_limit;
    }

    // generates a number from 0 - n exclusive of n
    public static int random(int n) {
        return rand.nextInt(n);
    }

    public static int randomNotEqual(int n, int not) {
        int r = rand.nextInt(n - 1);
        if (r == not) {
            if (r >= n - 1) {
                r--;
            } else {
                r++;
            }
        }
        return r;
    }

    // returns the Random object
    public static Random getRandom() {
        return rand;
    }

}
