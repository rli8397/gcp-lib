import general.Instance;
import papers.Garlinier1999Heuristic;
import papers.Hertz1987Heuristic;

import java.util.*;
import java.io.*;
public class main {
    public static void main(String[] args) {
       Instance instance = new Instance(new File("../test.txt"));
       Garlinier1999Heuristic heuristic = new Garlinier1999Heuristic(instance, 0, 5);
    }
}
