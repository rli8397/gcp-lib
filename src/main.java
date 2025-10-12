import general.Instance;
import general.HeuristicClasses.GCPHeuristic;
import papers.*;

import java.io.*;

public class main {
    public static void main(String[] args) {
        Instance instance = new Instance(new File("C:\\Users\\Ryan Li\\gcp-lib\\src\\testInstances\\test.txt"));
        // Heuristic heuristic = new Blochilger2008Heuristic(instance, 10);
        GCPHeuristic heuristic = new Garlinier1999Heuristic(instance, 10.0, 10); // paper says 10
    }
}
