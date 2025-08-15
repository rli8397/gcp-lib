import general.Heuristic;
import general.Instance;
import papers.Garlinier1999Heuristic;
import papers.Hertz1987Heuristic;
import papers.*;

import java.util.*;
import java.io.*;
public class main {
    public static void main(String[] args) {
       Instance instance = new Instance(new File("C:\\Users\\shrey\\OneDrive\\Documents\\GraphCol\\gcp-lib\\src\\testInstances\\le450_25d.col.txt"));

       System.out.println("NumEdges" + instance.getNumEdges());
       //Garlinier1999Heuristic heuristic = new Garlinier1999Heuristic(instance, 100, 5);

       Glass2003Heuristic heuristic2 = new Glass2003Heuristic(instance, 600, 100);

       heuristic2.printLog();
       System.out.println("Finished");


    }
}
