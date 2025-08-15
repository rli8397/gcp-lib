import general.Heuristic;
import general.Instance;

import java.util.*;

import Papers.*;

import java.io.*;
public class main {
    public static void main(String[] args) {
       Instance instance = new Instance(new File("C:\\Users\\Ryan Li\\gcp-lib\\src\\testInstances\\le450_15c.col"));
       Garlinier1999Heuristic heuristic = new Garlinier1999Heuristic(instance, 100, 5);
    //    Glass2003Heuristic heuristic2 = new Glass2003Heuristic(instance, 10, 5);

    //    heuristic.printLog();
       System.out.println("Finished");


    }
}
