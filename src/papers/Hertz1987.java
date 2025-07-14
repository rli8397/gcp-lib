package papers;

import general.*;
import java.util.*;

public class Hertz1987 {
    private Hertz1987Heuristic heuristic;
    public Hertz1987(Instance graph, double runtime, int tabuTenure, int rep) {
        heuristic = new Hertz1987Heuristic(graph, runtime, tabuTenure, rep);
    }

    public class Hertz1987Solution extends SolutionConflict {
        private double[] A;

        // constructor
        public Hertz1987Solution(Instance graph) {
            super(graph.getMaxChromatic(), graph, true, false);
            A = new double[graph.getNumEdges() + 1];
            A[(int)this.objective] = this.objective - 1;
        }

        // checks to see if a move is tabu based on the tabu map and the current
        // iteration
        public boolean isTabu(HashMap<Move, Integer> tabuMap, Move move, int iteration, int tenure) {
            return tabuMap.containsKey(move) && iteration - tabuMap.get(move) <= tenure;
        }

        public Move generateBestRepNeighbor(int rep, HashMap<Move, Integer> tabuMap, int iteration, int tenure) {
            double bestObj = Integer.MAX_VALUE;
            Move bestMove = null;
            int i = 0;
            int loopCount = 0;
            while (i < rep) {
                if (loopCount % 1000 == 0 && !heuristic.report()) {
                    break;
                }
                Move currMove = randConflictedMove();                
                double currObj = currMove.getObjective();
                if (!isTabu(tabuMap, currMove, iteration, tenure) || currObj <= A[(int) this.objective]) {
                    if (currObj < bestObj) {
                        bestObj = currObj;
                        bestMove = currMove;
                        if (bestObj < this.objective) {
                            A[(int) this.objective] = bestObj - 1;
                            break;
                        }
                    }
                    i += 1;
                }
                loopCount++;
            }

            // node pair store solution
            // objective = bestObj; // possibly store objective in node pair and add methods in solution that takes
                                 // a node pair, if that node pair has a objective, then use it, otherwise
                                 // calculate
            return bestMove;
        }

        // this is the tabucol local search that will run
        public void tabuSearch(int tabuTenure, int rep) {
            // Initialization
            // hash map where the keys are node and coloring parings, value is where the
            // pairing was made tabu
            HashMap<Move, Integer> tabuMap = new HashMap<>();
            int iteration = 0;
            Move lastMove = null;
            while (objective > 0 && heuristic.report()) { // possible optimization: calling heuristic report many times here can be very slow
                Move neighbor = generateBestRepNeighbor(rep, tabuMap, iteration, tabuTenure);
                if (lastMove != null) {
                    tabuMap.put(lastMove, iteration); 
                }
                lastMove = neighbor;
                makeMove(neighbor);
                iteration++;
            }
        }
    }

    public class Hertz1987Heuristic extends Heuristic {
        public Hertz1987Heuristic(Instance graph, double runtime, int tabuTenure, int rep) {
            super(graph, runtime);
            Hertz1987Solution solution = new Hertz1987Solution(graph);
            while (this.report()) {
                solution.reduceK(); // start with reducing k because finding a k coloring where k = n is trivial
                solution.tabuSearch(tabuTenure, rep);
                solution.printStatus();
            }

            System.out.println("runtime: " + this.getCurrRunTime());
            solution.printStatus();

        }
    }
}
