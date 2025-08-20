package papers;

import general.*;
import java.util.*;

// heuristic super class implement keeping track of the solution history
// Make the outer class the Hertz1987Heuristic to reduce overhead
public class Hertz1987Heuristic extends Heuristic {
    private int k;

    public Hertz1987Heuristic(Instance instance, double runtime, int tabuTenure, int rep) {
        super(instance, runtime);
        System.out.println("Heuristic Running");
        this.k = instance.getMaxChromatic();
        Hertz1987Solution solution = new Hertz1987Solution(this, instance, instance.getMaxChromatic());
        
        while (true) {
            solution.tabuSearch(tabuTenure, rep);

            if (!this.report(solution)) {
                break;
            }

            k--; 
            solution.redistributeColors(k);
        }

        System.out.println("runtime: " + this.getCurrRunTime());
        solution.printStatus();
    }

    public class Hertz1987Solution extends SolutionConflictCounts {

        public Hertz1987Solution(Heuristic heuristic, Instance instance, int k) {
            super(heuristic, Solution.randomColoring(heuristic, k), k);
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

            // keeping a simple static tenure
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
