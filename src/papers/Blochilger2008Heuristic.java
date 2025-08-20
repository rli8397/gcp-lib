package papers;

import java.util.*;
import java.util.BitSet;
import java.util.ArrayList;
import java.util.HashSet;

import general.Heuristic;
import general.Instance;

public class Blochilger2008Heuristic extends Heuristic {
    public Blochilger2008Heuristic(Instance instance, int runtime_limit) {
        super(instance, runtime_limit);
        Blochilger2008Solution solution = new Blochilger2008Solution(this, Blochilger2008Solution.partialColoring(this.instance, this.instance.getMaxChromatic(), this.rand), this.instance.getMaxChromatic());
        while (this.report(solution)) {
            solution.reduceK();
            solution.tabuSearch();
            solution.printStatus();
        }
        solution.printStatus();
    }

    public class Blochilger2008Solution extends SolutionConflictObjective {
        protected HashSet<Integer> uncolored;
        protected int[][] countsMatrix;

        public Blochilger2008Solution(Heuristic heuristic, int[] coloring, int colors) {
            super(heuristic, coloring, colors);
            uncolored = new HashSet<Integer>();
            for (int i = 0; i < coloring.length; i++) {
                if (coloring[i] == 0) {
                    uncolored.add(i);
                }
            }

            this.countsMatrix = new int[instance.getNumNodes()][k + 1];
            for (int n = 0; n < instance.getNumNodes(); n++) {
                HashSet<Integer> adj = instance.getAdjacent(n);
                for (int adjv : adj) {
                    if (coloring[n] > 0) {
                        this.countsMatrix[adjv][coloring[n]]++;
                    }
                }
            }
        }
        
        protected static int[] partialColoring(Instance instance, int k, Random rand) {
            int n = instance.getNumNodes();
            int[] coloring = new int[n];

            // convert adjancey set to bitset form
            List<BitSet> adjacencyList = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                BitSet bs = new BitSet(n);
                for (int neighbor : instance.getAdjacent(i)) {
                    bs.set(neighbor);
                }
                adjacencyList.add(bs);
            }

            BitSet[] classes = new BitSet[k];
            for (int i = 0; i < k; i++) {
                classes[i] = new BitSet(n);
            }

            List<Integer> vertices = Solution.randTraversalOrder(instance);
            for (int v : vertices) {
                boolean placed = false;
                for (int c = 1; c <= k; c++) {
                    if (!adjacencyList.get(v).intersects(classes[c])) {
                        coloring[v] = c; 
                        classes[c].set(v);
                        placed = true;
                        break;
                    }
                }

                if (!placed) {
                    coloring[v] = 0;
                }
            }
            return coloring;
        }

        public class BlochilgerTabuSearch extends TabuSearch {
            protected int currMaxObjective;
            protected int currMinObjective;
            protected int frequency;
            protected int threshold; 
            protected int increment;
            protected int tenure;

            public BlochilgerTabuSearch(int frequency, int threshold, int increment) {
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
                    this.tenure = Math.max(0, tenure - 1);
                }
            }

            public void dynTenure() {
                this.tenure = (int)(0.6 * uncolored.size() + heuristic.random(10));
            }
            public Move generateBestNeighbor(int iteration, SolutionConflictObjective solution) {
                Move bestMove = null;
                
                for (int node : uncolored) {
                    for (int color = 1; color <= k; color++) {
                        Move move = new Move(node, color, solution);
                        if (!isTabu(move, iteration)) {
                            if (bestMove == null || move.getObjective() < bestMove.getObjective()) {
                                bestMove = move;
                            }
                        }
                    }
                }

                return bestMove;
            }
        }

        public void calcObjective() {
            objective = this.uncolored.size();
        }
        
        public void calcNeighborObjective(Move move) {
            move.setObjective(this.objective - 1 + this.countsMatrix[move.node][move.color]);
        }

        public void makeMove(Move move, ArrayList<Move> removed) {
            this.uncolored.remove(move.node);
            this.coloring[move.node] = move.color;
            for (int neighbor : this.instance.getAdjacent(move.node)) {
                this.countsMatrix[neighbor][move.color]++;
                if (this.coloring[neighbor] == move.color) {
                    removed.add(new Move(neighbor, move.color, this));
                    this.uncolored.add(neighbor);
                    this.coloring[neighbor] = 0;
                    for (int adjv : this.instance.getAdjacent(neighbor)) {
                        this.countsMatrix[adjv][move.color]--;
                    }
                }
            }

            this.objective = move.getObjective();
            this.validSolution = this.objective == 0;
        }
        public void tabuSearch() {
            BlochilgerTabuSearch ts = new BlochilgerTabuSearch(10, 5, 2);
            int iteration = 0;
            while (objective > 0 && heuristic.report()) {
                Move move = ts.generateBestNeighbor(iteration, this);
                if (move == null) {
                    // reduce tabu tenure or something like that???
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