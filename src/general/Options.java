package general;

import java.util.ArrayList;

public class Options {
    public Instance instance;
    public long seed = System.currentTimeMillis();
    public double runtime = 10.0; //default runtime 
    public int verbosity = 0; //default verbosity level {silent = 0, normal = 1, verbose = 2, debug = 3}
    public ArrayList<String> extras = new ArrayList<>(); //list of extended parameters

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
