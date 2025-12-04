package general;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Options {
    public File instanceFile;                 
    public Instance instance;                 
    public long seed = System.currentTimeMillis();
    public double runtime = 10.0;             // default runtime
    public int verbosity = 0;                 

    // extras as a dictionary (key -> value)
    public Map<String, String> extras = new HashMap<>();

    public Options() {}

    @Override
    public String toString() {
        return "Options{" +
                "instance='" + instance + '\'' +
                ", seed=" + seed +
                ", runtime=" + runtime +
                ", versbosity =" + verbosity + "}";
    }

    //returns extra or returns null, up to file to parse string into desired datatype
    public String getExtra(String key){
        return extras.get(key);
    }
}
