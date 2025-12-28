package general;

import java.util.HashMap;

public class Options {
    public Instance instance;
    public long seed = System.currentTimeMillis();
    public double runtime = 10.0; //default runtime 
    public int verbosity = 0; //default verbosity level {silent = 0, normal = 1, verbose = 2, debug = 3}
    public HashMap<String, String> extras = new HashMap<>(); //map of extended parameters

    public Options() {}

    @Override
    public String toString() {
        return "Options{" +
                "instance='" + instance + '\'' +
                ", seed=" + seed +
                ", runtime=" + runtime +
                ", versbosity =" + verbosity + "}";
    }
}
