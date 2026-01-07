package papers;

import java.util.*;
import general.Instance;
import general.Move;
import general.HeuristicClasses.*;
import general.SolutionClasses.PartialColoring;
import general.SolutionClasses.Solution;
import general.Options;

public class Blochilger2008Heuristic extends GCPHeuristic {
    private int frequency;
    private int threshold;
    private int increment;
    private int initialTenure;

    public Blochilger2008Heuristic(Options options) {
        super(options);
        try {
            frequency = Integer.parseInt(options.extras.get("frequency"));
            threshold = Integer.parseInt(options.extras.get("threshold"));
            increment = Integer.parseInt(options.extras.get("increment"));
            initialTenure = Integer.parseInt(options.extras.get("initialTenure"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Missing or invalid extended parameters for Blochilger2008Heuristic. Required parameters: frequency, threshold, increment, initialTenure.");
        }

    }

    public void run() {
        int k = this.instance.getMaxChromatic();
        // instantiate solution with an initial partial coloring
        Blochilger2008Solution solution = new Blochilger2008Solution(this.getInstance(),
                Blochilger2008Solution.partialColoring(this, k), k);
        boolean revisitingK = false;
        while (true) {
            solution.tabuSearch();

            if (!this.report(solution.getBestSolution())) {
                break;
            }

            if (solution.isValidSolution()) {
                if (revisitingK) {
                    break;
                }
                k--;
            } else {
                // if no valid solution is found within max iterations, start revisitng k's
                // until
                // you find a valid solution, then stop
                revisitingK = true;
                k++;
            }
            // resets the coloring with the new k
            solution = new Blochilger2008Solution(this.getInstance(), Blochilger2008Solution.partialColoring(this, k), k);
        }
    }

    public class Blochilger2008Solution extends PartialColoring {
        protected int[][] countsMatrix;
        private int bestObjective = Integer.MAX_VALUE;
        private Solution bestSolution;
        
        public Blochilger2008Solution(Instance instance, int[] coloring, int k) {
            super(instance, coloring, k);
            this.countsMatrix = new int[instance.getNumNodes()][this.k + 1];
            for (int n = 0; n < instance.getNumNodes(); n++) {
                HashSet<Integer> adj = instance.getAdjacent(n);
                for (int adjv : adj) {
                    this.countsMatrix[adjv][coloring[n]]++;
                }
            }
            updateBestSolution();
        }

        public class BlochilgerTabuSearch extends TabuSearch<Move> {
            // to track the min and max objective in the last "frequency" moves
            protected int maxObj = Integer.MIN_VALUE;
            protected int minObj = Integer.MAX_VALUE;

            // used to mark moves tabu
            protected ArrayList<Move> removedMoves;
            
            protected int tenure;

            public BlochilgerTabuSearch(Blochilger2008Solution solution) {
                super(solution);
                this.tenure = initialTenure;
            }

            public void updateTenure() {
                // calculates tenure based on parameters
                if (maxObj - minObj <= threshold) {
                    this.tenure += increment;
                } else {
                    this.tenure = Math.max(0, tenure - 1); // ensure not negative
                }
                maxObj = Integer.MIN_VALUE;
                minObj = Integer.MAX_VALUE;
            }

            public void dynTenure() {
                this.tenure = (int) (0.6 * uncolored.size() + Heuristic.random(10));
            }

            public Move generateBestNeighbor(int iteration) {
                Move bestMove = null;

                for (int node : uncolored) {
                    for (int color = 1; color <= k; color++) {
                        Move move = new Move(node, color, solution);
                        if (!isTabu(move, iteration) || move.getObjective() < bestObjective) {
                            // techianlly for ties should pick randomly among best, but it might not be
                            // worth, but making a note for now
                            if (bestMove == null || move.getObjective() < bestMove.getObjective()) {
                                bestMove = move;
                            }
                        }
                    }
                }

                return bestMove;
            }

            public void makeMove(Move move) {
                removedMoves = new ArrayList<>();
                // Remove the node from uncolored and assign the color
                uncolored.remove(move.getNode());
                coloring[move.getNode()] = move.getColor();

                // Update counts matrix for all neighbors of the moved node
                for (int neighbor : instance.getAdjacent(move.getNode())) {
                    countsMatrix[neighbor][move.getColor()]++;

                    // If neighbor has the same color, it creates a conflict - uncolor it
                    if (coloring[neighbor] == move.getColor()) {
                        removedMoves.add(new Move(neighbor, move.getColor(), solution));
                        uncolored.add(neighbor);
                        coloring[neighbor] = 0;

                        // Update counts matrix by removing the neighbor's color influence
                        for (int adjv : instance.getAdjacent(neighbor)) {
                            countsMatrix[adjv][move.getColor()]--;
                        }
                    }
                }

                // Recalculate objective to ensure it matches uncolored.size()
                updateObjective();
                updateBestSolution();
                minObj = Math.min(minObj, objective);
                maxObj = Math.max(maxObj, objective);
            }

            public void tabuAppend(Move move, int iteration) {
                for (Move m : removedMoves) {
                    tabuMap.put(m, iteration + getTenure());
                }
                removedMoves = new ArrayList<>();
            }
        }

        public void updateObjective() {
            objective = this.uncolored.size();
        }

        public void calcNeighborObjective(Move move) {
            move.setObjective(this.objective - 1 + this.countsMatrix[move.getNode()][move.getColor()]);
        }

        public void setColoring(int[] coloring, int k) {
            this.coloring = coloring;
            this.k = k;
        }

        public void updateBestSolution() {
            if (objective < bestObjective) {
                this.bestObjective = objective;
                this.bestSolution = new PartialColoring(this.instance, this.coloring.clone(), this.k);
            }
        }

        public Solution getBestSolution() {
            return this.bestSolution;
        }

        public void tabuSearch() {
            // frequency - [500:5000]
            // threshold - [5: 30]
            // increment - [1:2]
            BlochilgerTabuSearch ts = new BlochilgerTabuSearch(this);
            int maxIterations = 10000;
            int iteration = 0;

            while (uncolored.size() > 0 && iteration < maxIterations) {
                Move move = ts.generateBestNeighbor(iteration);

                if (move == null) {
                    break; // No valid moves available
                }

                if ((iteration + 1) % frequency == 0) {
                    ts.updateTenure();
                }

                if (move.getObjective() < this.bestObjective) {
                    iteration = 0;
                }

                ts.makeMove(move);

                ts.tabuAppend(move, iteration);

                iteration++;
            }
        }
    }
}