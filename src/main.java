import general.Heuristic;
import general.Instance;
import java.io.*;
import java.util.*;

import papers.Hertz1987;
import papers.Hertz1987.*;
public class main {
    public static void main(String[] var0) {
        Instance instance = new Instance(new File("../test.txt"));
        Hertz1987 run = new Hertz1987(instance, 10, 7, 3);
    }
}
