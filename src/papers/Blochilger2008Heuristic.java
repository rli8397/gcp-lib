/* 
package papers;

import java.io.ObjectStreamConstants;
import java.util.*;

import general.Instance;
import general.Move;
import general.HeuristicClasses.*;
import general.SolutionClasses.PartialColoring;
import general.SolutionClasses.Solution;
import general.Options;

public class Blochilger2008Heuristic extends Heuristic {
    public Blochilger2008Heuristic(Options options) {
        super(options);
        Blochilger2008Solution solution = new Blochilger2008Solution(this,
                                                Blochilger2008Solution.partialColoring(this, this.k), this.k);
        while (true) {
            solution.tabuSearch();

            if (!this.report(solution)) {
                break;
            }
            this.k--;
            solution.reduceK();
        }
    }

    public class Blochilger2008Solution extends PartialColoring {
        protected int[][] countsMatrix;
        private int bestObjective;

        public Blochilger2008Solution(Heuristic heuristic, int[] coloring, int colors) {
            super(heuristic, coloring, colors);
            this.countsMatrix = new int[instance.getNumNodes()][k + 1];
            for (int n = 0; n < instance.getNumNodes(); n++) {
                HashSet<Integer> adj = instance.getAdjacent(n);
                for (int adjv : adj) {
                    this.countsMatrix[adjv][coloring[n]]++;
                }
            }
            calcObjective();
            bestObjective = objective;
        }

        public class BlochilgerTabuSearch extends TabuSearch<Move> {
            protected int currMaxObjective;
            protected int currMinObjective;
            protected int frequency;
            protected int threshold;
            protected int increment;
            protected int tenure;

            public BlochilgerTabuSearch(int frequency, int threshold, int increment, Blochilger2008Solution solution) {
                initTabu(solution);
                this.currMaxObjective = objective;
                this.currMinObjective = objective;
                this.frequency = frequency;
                this.threshold = threshold;
                this.increment = increment;
            }

            public void updateTenure() {
                // updates delta first
                if (objective > this.currMaxObjective) {
                    this.currMaxObjective = objective;
                } else if (objective < this.currMinObjective) {
                    this.currMinObjective = objective;
                }

                // calculates tenure based on parameters
                if (this.currMaxObjective - this.currMinObjective <= this.threshold) {
                    this.tenure += this.increment;
                } else {
                    this.tenure = Math.max(0, tenure - 1); // ensure not negative
                }
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
                            if (bestMove == null || move.getObjective() < bestMove.getObjective()) {
                                bestMove = move;
                            }
                        }
                    }
                }

                return bestMove;
            }

            public void tabuAppend(Move move, int iteration) {
                Move tabuMove = new Move(move.getNode(), solution.getColoring()[move.getNode()], solution);
                // a move is still tabu as long as the iteration is 
                // <= curr iteration + tabuTenure
                tabuMap.put(tabuMove, iteration + getTenure());
            } 
        }

        public void calcObjective() {
            objective = this.uncolored.size();
        }

        public void calcNeighborObjective(Move move) {
            move.setObjective(this.objective - 1 + this.countsMatrix[move.getNode()][move.getColor()]);
        }

        public void makeMove(Move move, ArrayList<Move> removed) {
            // Remove the node from uncolored and assign the color
            this.uncolored.remove(move.getNode());
            this.coloring[move.getNode()] = move.getColor();
            
            // Update counts matrix for all neighbors of the moved node
            for (int neighbor : this.instance.getAdjacent(move.getNode())) {
                this.countsMatrix[neighbor][move.getColor()]++;
                
                // If neighbor has the same color, it creates a conflict - remove it
                if (this.coloring[neighbor] == move.getColor()) {
                    removed.add(new Move(neighbor, move.getColor(), this));
                    this.uncolored.add(neighbor);
                    this.coloring[neighbor] = 0;
                    
                    // Update counts matrix by removing the neighbor's color influence
                    for (int adjv : this.instance.getAdjacent(neighbor)) {
                        this.countsMatrix[adjv][move.getColor()]--;
                    }
                }
            }

            // Recalculate objective to ensure it matches uncolored.size()
            calcObjective();
            
            if (this.objective < this.bestObjective) {
                this.bestObjective = this.objective;
            }
        }

        public void reduceK() {
            // First, update the counts matrix to remove the influence of color k
            for (int i = 0; i < coloring.length; i++) {
                if (this.coloring[i] == k) {
                    // Remove this node's color influence from neighbors' counts
                    for (int neighbor : this.instance.getAdjacent(i)) {
                        this.countsMatrix[neighbor][k]--;
                    }
                }
            }
            
            // Now reassign nodes that were using color k
            for (int i = 0; i < coloring.length; i++) {
                if (this.coloring[i] == k) {
                    int[] usedColors = new int[k]; // Note: k-1 colors now available (1 to k-1)
                    for (int advj : this.instance.getAdjacent(i)) {
                        if (this.coloring[advj] > 0 && this.coloring[advj] < k) {
                            usedColors[this.coloring[advj]]++;
                        }
                    }

                    int newColor = 0;
                    for (int j = 1; j < k; j++) { // k-1 is now the max color
                        if (usedColors[j] == 0) {
                            newColor = j;
                            break;
                        }
                    }

                    this.coloring[i] = newColor;
                    if (newColor == 0) {
                        this.uncolored.add(i);
                    } else {
                        // Add this node's color influence back to neighbors' counts
                        for (int neighbor : this.instance.getAdjacent(i)) {
                            this.countsMatrix[neighbor][newColor]++;
                        }
                    }
                }
            }
            
            k--;
            
            // Resize counts matrix to remove the last color column
            int[][] newCountsMatrix = new int[instance.getNumNodes()][k + 1];
            for (int i = 0; i < instance.getNumNodes(); i++) {
                System.arraycopy(this.countsMatrix[i], 0, newCountsMatrix[i], 0, k + 1);
            }
            this.countsMatrix = newCountsMatrix;
            
            calcObjective();
            bestObjective = objective;
        }

        public void tabuSearch() {
            BlochilgerTabuSearch ts = new BlochilgerTabuSearch(10, 5, 2, this);
            int iteration = 0;

            while (objective > 0 && heuristic.report()) {
                Move move = ts.generateBestNeighbor(iteration);
                if (move == null) {
                    break; // No valid moves available
                }
                
                ArrayList<Move> removed = new ArrayList<>();
                makeMove(move, removed);
                ts.updateTenure();

                for (Move m : removed) {
                    ts.tabuMap.put(m, iteration + ts.tenure);
                }

                iteration++;
            }
        }
    }
}
    */