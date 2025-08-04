package papers;

import general.*;
import java.util.*;

// heuristic super class implement keeping track of the solution history
// Make the outer class the Hertz1987Heuristic to reduce overhead
public class Hertz1987Heuristic extends Heuristic {
    public Hertz1987Heuristic(Instance graph, double runtime, int tabuTenure, int rep) {
        super(graph, runtime);
        System.out.println("Heuristic Running");
        Hertz1987Solution solution = new Hertz1987Solution(this, graph);
        while (this.report(solution)) { // report maybe receives a solution
            solution.reduceK();
            solution.tabuSearch(tabuTenure, rep);
        }

        System.out.println("runtime: " + this.getCurrRunTime());
        solution.printStatus();
    }

    public class Hertz1987Solution extends SolutionConflictCounts {
        // constructor
        public Hertz1987Solution(Heuristic heuristic, Instance instance) {
            super(heuristic, Solution.randomColoring(instance.getNumNodes(), instance.getMaxChromatic(),
                    heuristic.getRandom()), instance.getMaxChromatic());
            this.conflictCount = new int[instance.getNumNodes() + 1];
        }

        public class HertzTabuSearch extends TabuSearch {
            public HertzTabuSearch(int tenure, SolutionConflictCounts solution) {
                this.tenure = tenure;
                this.solution = solution;
                tabuMap = new HashMap<>();
                this.A = new int[instance.getNumEdges() + 1];
                for (int i = 0; i < A.length; i++) {
                    this.A[i] = i - 1;
                }
            }

            public void updateTenure() {

            }

        }

        // this is the tabucol local search that will run
        public void tabuSearch(int tabuTenure, int rep) {
            // Initialization
            // hash map where the keys are node and coloring parings, value is where the
            // pairing was made tabu
            HertzTabuSearch ts = new HertzTabuSearch(tabuTenure, this);
            int iteration = 0;
            while (objective > 0 && heuristic.report()) { // possible optimization: calling heuristic report many
                                                              // times might be slow
                Move neighbor = ts.generateBestRepNeighbor(rep, iteration);
                Move tabuMove = new Move(neighbor.node, this.coloring[neighbor.node], this);
                ts.tabuMap.put(tabuMove, iteration + tabuTenure); // a move is still tabu as long as the iteration is <=
                                                                  // curr iteration + tabuTenure
                makeMove(neighbor);
                iteration++;
            }
        }
    }

}
