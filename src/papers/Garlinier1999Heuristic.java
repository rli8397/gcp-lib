package papers;

import general.Instance;
import general.Move;
import general.HeuristicClasses.*;

public class Garlinier1999Heuristic extends GASkeleton {
    public Garlinier1999Heuristic(Instance instance, double runtime, int popSize) {
        super(instance, runtime, popSize);
    }

    public class Garlinier1999Solution extends GASkeleton.GASkeletonSolution {
        // instantiation will leave solution floating
        // would instantiate either with a greedy or a crossover, like factory
        public Garlinier1999Solution(GCPHeuristic heuristic, int[] coloring, int colors) {
            super(heuristic, coloring, colors);
        }

        public class GarlinierTabuSearch extends TabuSearch<Move> {
            private int a;
            private double alpha;
            private int rep;
            private int iterations;

            public GarlinierTabuSearch(int a, double alpha, int rep, int iterations, Garlinier1999Solution solution) {
                initTabu(solution);
                this.a = a;
                this.alpha = alpha;
            }

            public int getTenure() {
                return (int) (GCPHeuristic.random(a) + alpha * nb_cfl);
            }

            public Move generateBestNeighbor(int iteration) {
                int bestObj = Integer.MAX_VALUE;
                Move bestMove = null;
                int i = 0;
                int loopCount = 1;

                while (i < rep) {
                    // if 1000 iterations have passed, we are assuming that a neighbor can't with
                    // the given criteria and are going
                    // to return, this is a judgement call and is not stated in the paper
                    if (loopCount % 1000 == 0) {
                        // if no move has been made, return any random move, other return the best move
                        // found so far
                        if (i == 0) {
                            return solution.randMove();
                        } else {
                            return bestMove;
                        }
                    }

                    Move currMove = randConflictedMove();
                    int currObj = currMove.getObjective();
                    if (!isTabu(currMove, iteration) || currObj <= A[objective]) {
                        if (currObj < bestObj) {
                            bestObj = currObj;
                            bestMove = currMove;
                            if (bestObj < objective) {
                                A[objective] = bestObj - 1;
                                break;
                            }
                        }
                        i += 1;
                    }
                    loopCount++;

                }

                return bestMove;
            }

            
            public void tabuAppend(Move move, int iteration) {
                Move tabuMove = new Move(move.getNode(), solution.getColoring()[move.getNode()], solution);
                // a move is still tabu as long as the iteration is 
                // <= curr iteration + tabuTenure
                tabuMap.put(tabuMove, iteration + getTenure());
            }

            
            public boolean stopCondition(int iteration) {
                return solution.isValidSolution() || iteration >= iterations || !heuristic.report();
            }
        }

        // this is different than the normal calc objective because it also updates
        // nb_cfl which will be used to
        // determine the length of the tabu tenure at each iteration

        public void localSearch() {
            // read more about tabu tenure and rep count

            // parameters
            int iterations = 1000;
            int rep = 5;
            int A = 10;
            double alpha = 0.6;
            
            GarlinierTabuSearch ts = new GarlinierTabuSearch(A, alpha, rep, iterations, this);
            ts.tabuSearch();

            // while (objective > 0 && iteration < iterations && heuristic.report()) {
            //     Move neighbor = ts.generateBestNeighbor(rep, iteration, this);
            //     Move tabuMove = new Move(neighbor.node, this.coloring[neighbor.node], this);
            //     ts.tabuMap.put(tabuMove, iteration + ts.getTenure()); // a move is still tabu as long as the iteration
            //                                                           // is <=
            //     // curr iteration + tabuTenure
            //     iteration++;
            // }
        }

    }
}
