import general.Heuristic;
import general.Instance;

import java.util.*;

import papers.*;

import java.io.*;
public class main {
    public static void main(String[] args) {
        Instance instance = new Instance(new File("C:\\Users\\Ryan Li\\gcp-lib\\src\\testInstances\\fpsol2.i.1.col"));
        Heuristic heuristic = new Garlinier1999Heuristic(instance, 10, 10);


    }
}
