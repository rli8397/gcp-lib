package papers;

import general.*;
import java.util.*;

// heuristic super class implement keeping track of the solution history
// Make the outer class the Hertz1987Heuristic to reduce overhead
public class Hertz1987 {
    private Solution bestSolution;
    private Solution bestValidSolution;

    public Hertz1987(Instance graph, double runtime, int tabuTenure, int rep) {
        Heuristic heuristic = new Hertz1987Heuristic(graph, runtime, tabuTenure, rep);
    }

    public Solution getBestSolution() {
        return bestSolution;
    }

    public Solution getBestValidSolution() {
        return bestValidSolution;
    }

    public class Hertz1987Solution extends SolutionConflict {
        private double[] A;

        // constructor
        public Hertz1987Solution(Heuristic heuristic, Instance graph) {
            super(heuristic, graph.getMaxChromatic(), graph, true, false);
            A = new double[graph.getNumEdges() + 1];
            for (int i = 0; i < A.length; i++) {
                A[i] = i - 1;
            }
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
                    // if loop count and i is still 0
                    // return any random move
                    break; // note if break bestMove will be null
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
            // objective = bestObj; // possibly store objective in node pair and add methods
            // in solution that takes
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
            while (objective > 0 && heuristic.report()) { // possible optimization: calling heuristic report many times
                                                          // here can be very slow
                Move neighbor = generateBestRepNeighbor(rep, tabuMap, iteration, tabuTenure);
                Move tabuMove = new Move(neighbor.node, this.coloring[neighbor.node], this);
                tabuMap.put(tabuMove, iteration);
                makeMove(neighbor);
                iteration++;
            }
        }
    }

    public class Hertz1987Heuristic extends Heuristic {
        public Hertz1987Heuristic(Instance graph, double runtime, int tabuTenure, int rep) {
            super(graph, runtime);
            Hertz1987Solution solution = new Hertz1987Solution(this, graph);
            while (this.report()) { // report maybe receives a solution
                solution.reduceK(); // start with reducing k because finding a k coloring where k = n is trivial
                solution.tabuSearch(tabuTenure, rep);
                if (solution.objective == 0) {
                    bestValidSolution = solution;
                }
                solution.printStatus();
            }

            bestSolution = solution;
            System.out.println("runtime: " + this.getCurrRunTime());
            solution.printStatus();
        }
    }
}
