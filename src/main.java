import general.Instance;
import general.HeuristicClasses.GCPHeuristic;
import papers.*;
import general.SolutionClasses.*;
import java.io.*;
import general.Move;

public class main {
    public static void main(String[] args) {
        Instance instance = new Instance(new File("C:\\Users\\Ryan Li\\gcp-lib\\src\\testInstances\\test.txt"));
        GCPHeuristic heuristic = new Hertz1987Heuristic(instance, 10.0); // paper says 10
        // int[] coloring = {1, 2, 2, 1, 1};
        // SolutionConflictObjective solution = new SolutionConflictObjective(instance, coloring, 2);
        // solution.makeMove(new Move(3, 2, solution));
        // System.out.println(solution.get_nb_cfl());
    }
}
