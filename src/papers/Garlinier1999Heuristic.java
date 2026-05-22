package papers;

import general.HeuristicClasses.*;
import general.*;

/*
    Notes: Objective is the number of edges having both endpoints in the same class
    
 */
public class Garlinier1999Heuristic extends GASkeleton {
    private int a;
    private double alpha;
    private int rep;
    private int maxIterations;

    public Garlinier1999Heuristic(Options options) {
        super(options);
        try {
            this.a = Integer.parseInt(get_cmdline_arg("a"));
            this.alpha = Double.parseDouble(get_cmdline_arg("alpha"));
            this.rep = Integer.parseInt(get_cmdline_arg("rep"));
            this.maxIterations = Integer.parseInt(get_cmdline_arg("maxiterations"));
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Missing or invalid extended parameters for Garlinier1999Heuristic. Required parameters: a, alpha, rep, maxiterations.");
        }
    }

    public GASkeletonSolution instantiateSolution(Instance instance, int[] coloring, int k) {
        Garlinier1999Solution sol = new Garlinier1999Solution(instance, coloring, k);
        sol.localSearch();
        return sol.bestSolution;
    }

    public class Garlinier1999Solution extends GASkeleton.GASkeletonSolution {
        private Garlinier1999Solution bestSolution;

        // instantiation will leave solution floating
        // would instantiate either with a greedy or a crossover, like factory
        public Garlinier1999Solution(Instance instance, int[] coloring, int k) {
            super(instance, coloring, k);
        }

        public Garlinier1999Solution(Garlinier1999Solution other) {
            super(other);
        }

        public class GarlinierTabuSearch extends HertzTabuSearch {

            public GarlinierTabuSearch(Garlinier1999Solution solution) {
                super(solution, Garlinier1999Heuristic.this);
                bestSolution = new Garlinier1999Solution(solution);
            }

            public int getTenure() {
                return (int) (GCPHeuristic.random(a) + alpha * nb_cfl);
            }

            public Move generateBestNeighbor(int iteration) {
                Move bestMove = generateBestRepNeighbor(iteration, rep);
                // in order to keep track of the best solution found overall
                if (bestMove != null && bestMove.getObjective() <= bestSolution.getObjective()) {
                    bestSolution = new Garlinier1999Solution(Garlinier1999Solution.this);
                    bestSolution.makeMove(bestMove);
                }
                return bestMove;
            }

            public boolean stopCondition(int iteration) {
                return solution.isValidSolution() || iteration >= maxIterations || !heuristic.report();
            }
        }

        public void localSearch() {
            GarlinierTabuSearch ts = new GarlinierTabuSearch(this);
            ts.tabuSearch();
        }

    }
}
