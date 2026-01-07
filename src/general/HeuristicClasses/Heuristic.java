package general.HeuristicClasses;

import java.util.*;
import general.*;

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
    protected HashMap<String, String> cmdline_params;
    protected static Random rand = new Random(1);

    public Heuristic(Options options) {
        this.instance = options.instance;
        this.runtime_limit = options.runtime;
        this.start_time = System.currentTimeMillis();
        cmdline_params = options.extras;
        setRandSeed(options.seed);
    }

    public Heuristic(Heuristic other) {
        this.instance = other.instance;
        this.runtime_limit = other.runtime_limit;
        this.start_time = System.currentTimeMillis();
        this.cmdline_params = other.cmdline_params;
    }

    // to be deleted
    public Heuristic(Instance instance, double runtime) {
        this.instance = instance;
        this.runtime_limit = runtime;
        this.start_time = System.currentTimeMillis();
    }

    public static synchronized void setRandSeed(long seed) {
        rand.setSeed(seed);
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

    // generates a number from 0 - n exclusive of n, and not "not"
    public static int randomNotEqual(int n, int not) {
        // for an invalid not, just return random number
        if (not < 0 || not >= n) {
            System.out.println("Warning in randomNotEqual: not value out of range");
            return random(n);
        }

        int r = rand.nextInt(n - 1);
        if (r >= not) {
            r++;
        }
        return r;
    }

    public String get_cmdline_arg(String key) {
        return cmdline_params.get(key);
    }

    // returns the Random object
    public static Random getRandom() {
        return rand;
    }

}
