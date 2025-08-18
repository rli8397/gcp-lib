package papers;

import java.util.HashSet;
import java.util.HashMap;

import general.Heuristic;
import general.Instance;

public class Garlinier1999Heuristic extends GASkeleton {
    public Garlinier1999Heuristic(Instance instance, double runtime, int popSize) {
        super(instance, runtime, popSize);
    }

    public class Garlinier1999Solution extends GASkeleton.GASkeletonSolution {
        private int nb_cfl;

        // instantiation will leave solution floating
        // would instantiate either with a greedy or a crossover, like factory
        public Garlinier1999Solution(Heuristic heuristic, int[] coloring, int colors) {
            super(heuristic, coloring, colors);
            TabuSearch(1000, 5, 4, 3);
        }

        public class GarlinierTabuSearch extends TabuSearch {
            private int a;
            private int alpha;

            public GarlinierTabuSearch(int a, int alpha, Garlinier1999Solution solution) {
                tabuMap = new HashMap<>();
                this.a = a;
                this.alpha = alpha;
                this.solution = solution;
                this.A = new int[instance.getNumEdges() + 1];
                for (int i = 0; i < A.length; i++) {
                    this.A[i] = i - 1;
                }
            }

            public int getTenure() {
                return Heuristic.random(a) + alpha * nb_cfl;
            }
        }

        // this is different than the normal calc objective because it also updates
        // nb_cfl which will be used to
        // determine the length of the tabu tenure at each iteration
        public void calcObjective() {
            int obj = 0;
            for (int i = 0; i < coloring.length; i++) {
                HashSet<Integer> adj = this.instance.getAdjacent(i);
                boolean conflictedNode = false;
                for (int adjv : adj) {
                    // If i < adjv, that edge hasn't been checked yet, this prevents from double
                    // counting
                    if (coloring[i] == coloring[adjv]) {
                        if (i < adjv) {
                            obj += 1;
                        }
                        if (!conflictedNode) {
                            nb_cfl++; // maybe consider making conflict count solution count the conflict vertices
                            conflictedNode = true;
                        }
                    }
                }
            }
            objective = obj;
            validSolution = objective == 0;
        }

        public void TabuSearch(int iterations, int rep, int A, int alpha) {
            // read more about tabu tenure and rep count
            GarlinierTabuSearch ts = new GarlinierTabuSearch(A, alpha, this);
            ts.solution.heuristic = this.heuristic;
            int iteration = 0;

            while (objective > 0 && iteration < iterations && heuristic.report()) {
                Move neighbor = ts.generateBestRepNeighbor(rep, iteration);
                Move tabuMove = new Move(neighbor.node, this.coloring[neighbor.node], this);
                ts.tabuMap.put(tabuMove, iteration + ts.getTenure()); // a move is still tabu as long as the iteration is <=
                                                                 // curr iteration + tabuTenure
                iteration++;
            }
        }

    }
}
