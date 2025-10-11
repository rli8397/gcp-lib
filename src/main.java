import general.Instance;
import general.HeuristicClasses.GCPHeuristic;

import java.util.*;

import papers.*;

import java.io.*;
public class main {
    public static void main(String[] args) {
        Instance instance = new Instance(new File("C:\\Users\\Ryan Li\\gcp-lib\\src\\testInstances\\fpsol2.i.1.col"));
        // Heuristic heuristic = new Blochilger2008Heuristic(instance, 10);
        GCPHeuristic heuristic = new Garlinier1999Heuristic(instance, 100, 10); // paper says 10


    }
}
