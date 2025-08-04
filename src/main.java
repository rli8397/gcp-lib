import general.Heuristic;
import general.Instance;
import papers.Garlinier1999Heuristic;
import papers.Hertz1987Heuristic;

import java.util.*;
import java.io.*;
public class main {
    public static void main(String[] args) {
       Instance instance = new Instance(new File("C:/Users/Ryan Li/gcp-lib/src/testInstances/fpsol2.i.1.col"));
       Garlinier1999Heuristic heuristic = new Garlinier1999Heuristic(instance, 60, 5);
    }
}
