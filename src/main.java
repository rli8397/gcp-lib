import java.io.File;
import general.Instance;
import papers.Chams1987Solution;
import papers.Hertz1987Solution;
import general.Heuristic;

import general.Instance;
import papers.SolutionConflict;

public class main{
    public static void main(String[] args){
<<<<<<< HEAD
        File test= new File ("C:\\Users\\shrey\\OneDrive\\Documents\\GraphCol\\gcp-lib\\test.txt");
        Instance testergraph  = new Instance (test);

        

=======
        Instance graph = new Instance(new File("../test.txt"));
        Chams1987Solution solution = new Chams1987Solution(graph.getNumNodes(), graph);
        solution.Chams1987(new Heuristic(graph,10));
>>>>>>> 7c8a517d7a958b271909a98b227900cfd7d8809c
    }
}