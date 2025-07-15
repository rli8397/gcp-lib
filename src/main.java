import java.io.File;
import general.Instance;
import papers.Chams1987Solution;
import papers.Hertz1987Solution;
import general.Heuristic;

import general.Instance;
import papers.SolutionConflict;

public class main{
    public static void main(String[] args){
        Instance graph = new Instance(new File("../test.txt"));
        Chams1987Solution solution = new Chams1987Solution(graph.getNumNodes(), graph);
        solution.Chams1987(new Heuristic(graph,10));
    }
}