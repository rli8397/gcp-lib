package papers;

import general.Instance;
import general.HeuristicClasses.GCPHeuristic;
import general.*;

import java.util.*;

public class Glass2003Heuristic extends GASkeleton {

    public Glass2003Heuristic(Options options) {
        super(options);
    }

    public GASkeletonSolution instantiateSolution(Instance instance, int[] coloring, int colors) {
        return new Glass2003Solution(instance, coloring, colors);
    }
    
    public class Glass2003Solution extends GASkeleton.GASkeletonSolution {

        public Glass2003Solution(Instance instance, int[] coloring, int colors) {
            super(instance, coloring, colors);
            // CHANGE VERTEX DESCENT SO THAT IT DOESN'T CALL CALCOBJECTIVE AND JUST ALTERS
            // THE OBJECTIVE ALREADY THERE
            localSearch();
        }

        public void localSearch() {
            int numNodes = instance.getNumNodes();
            int[][] costMatrix = new int[numNodes + 1][k + 1];

            // Best color list for each 1-indexed vertex.
            ArrayList<Integer>[] bestColorList = new ArrayList[numNodes + 1];

            for (int i = 1; i <= numNodes; i++) {
                bestColorList[i] = new ArrayList<Integer>();
                recomputeBestColorsForVertex(i, costMatrix, bestColorList);
            }

            boolean changed = true;

            // Cycles until no changes can be made
            while (changed && report()) {
                changed = false;
                for (int i = 1; i <= numNodes; i++) {
                    int currentColor = coloring[i];

                    if (!bestColorList[i].isEmpty() && !bestColorList[i].contains(currentColor)) {
                        int newColor = bestColorList[i].get(GCPHeuristic.random(bestColorList[i].size()));
                        coloring[i] = newColor;
                        changed = true;

                        // Update affected vertices only: moved vertex and its neighbors.
                        recomputeBestColorsForVertex(i, costMatrix, bestColorList);
                        for (int neighbor : instance.getAdjacent(i)) {
                            recomputeBestColorsForVertex(neighbor, costMatrix, bestColorList);
                        }
                    }
                }
            }

            // Finally update the objective and conflict counters
            // calcObjective();
        }

        private void recomputeBestColorsForVertex(int vertex, int[][] costMatrix, ArrayList<Integer>[] bestColorList) {
            int minConflict = Integer.MAX_VALUE;
            bestColorList[vertex].clear();

            for (int c = 1; c <= k; c++) {
                costMatrix[vertex][c] = computeCost(vertex, c);
                if (costMatrix[vertex][c] < minConflict) {
                    bestColorList[vertex].clear();
                    bestColorList[vertex].add(c);
                    minConflict = costMatrix[vertex][c];
                } else if (costMatrix[vertex][c] == minConflict) {
                    bestColorList[vertex].add(c);
                }
            }
        }

        //Computes Cost of each move
        private int computeCost(int vertex, int color) {
            int cost = 0;
            for (int neighbor : instance.getAdjacent(vertex)) {
                if (coloring[neighbor] == color) {
                    cost++;
                }
            }
            return cost;
        }


        public void calcMaxCardinalityClass() {
            int[] counts = new int[this.k];
            int maxCardinality = -1;
            for (int i = 1; i <= instance.getNumNodes(); i++) {
                if (this.coloring[i] > 0) {
                    counts[this.coloring[i]-1]++;
                    if (counts[this.coloring[i]-1] > maxCardinality) {
                        maxCardinality = counts[this.coloring[i]-1];
                    }
                }
            }
        }

       

    }


}