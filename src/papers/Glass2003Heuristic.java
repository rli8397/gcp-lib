package papers;

import general.Instance;
import general.HeuristicClasses.GCPHeuristic;
import general.SolutionClasses.SolutionConflictCounts;

import java.util.*;

public class Glass2003Heuristic extends GASkeleton {

    public Glass2003Heuristic(Instance instance, double runtime, int popSize) {
        super(instance, runtime, popSize);
    }

    public class Glass2003Solution extends SolutionConflictCounts{

        public Glass2003Solution(GCPHeuristic heuristic, int[] coloring, int colors) {
            super(heuristic, coloring, colors);
            calcObjective(); // the SolutionConflcit Objective calls this so its redundant here I think

            // CHANGE VERTEX DESCENT SO THAT IT DOESN'T CALL CALCOBJECTIVE AND JUST ALTERS
            // THE OBJECTIVE ALREADY THERE
            vertexDescent();
        }

        public void vertexDescent() {

            int numNodes = instance.getNumNodes();
            int[][] costMatrix = new int[numNodes][k];

            // Best color List Structure for each vertex
            ArrayList<Integer>[] bestColorList = new ArrayList[numNodes];

            int minConflict = Integer.MAX_VALUE;

            // Initialize cost matrix and best color list
            for (int i = 0; i < numNodes; i++) {
                bestColorList[i] = new ArrayList<Integer>();

                // Compute Cost of each Vertex if Moved to different color
                for (int c = 1; c <= k; c++) {
                    costMatrix[i][c - 1] = computeCost(i, c);

                    if (costMatrix[i][c - 1] < minConflict) {
                        bestColorList[i].clear();
                        bestColorList[i].add(c);
                        minConflict = costMatrix[i][c - 1];
                    } else if (costMatrix[i][c - 1] == minConflict) {
                        bestColorList[i].add(c);
                    }
                }

            }

            boolean changed = true;

            // Cycles until no changes can be made
            while (changed && heuristic.report()) {

                changed = false;
                for (int i = 0; i < numNodes; i++) {
                    int currentColor = coloring[i];
                    minConflict = Integer.MAX_VALUE;

                    if (bestColorList[i].size() > 0 && !bestColorList[i].contains(currentColor)) {
                        int newColor = bestColorList[i].get(GCPHeuristic.random(bestColorList[i].size()));
                        coloring[i] = newColor;
                        changed = true;

                        // Update cost matrix and bestColorList for neighbors
                        for (int neighbor : instance.getAdjacent(i)) {
                            // might want to try separating this out into a separate function just for
                            // organization
                            // i think i remember if you pass costMatrix and bestColorList it will modify
                            // the original ones because it makes
                            // a copy of the reference not the actual array
                            // you could name it like updateNodeCost or something
                            for (int c = 1; c <= k; c++) {
                                costMatrix[neighbor][c - 1] = computeCost(neighbor, c);

                                if (costMatrix[neighbor][c - 1] < minConflict) {
                                    bestColorList[neighbor].clear();
                                    bestColorList[neighbor].add(c);
                                    minConflict = costMatrix[neighbor][c - 1];

                                } else if (costMatrix[neighbor][c - 1] == minConflict) {
                                    bestColorList[neighbor].add(c);
                                }
                            }
                        }

                        // Update own row too
                        for (int c = 1; c <= k; c++) {
                            costMatrix[i][c - 1] = computeCost(i, c);

                            if (costMatrix[i][c - 1] < minConflict) {
                                bestColorList[i].clear();
                                bestColorList[i].add(c);
                                minConflict = costMatrix[i][c - 1];

                            } else if (costMatrix[i][c - 1] == minConflict) {
                                bestColorList[i].add(c);
                            }
                        }
                    }
                }
            }

            // Finally update the objective and conflict counters
            calcObjective();
        }

        private void updateNodeCost(int node, int[][] costMatrix, ArrayList<Integer>[] bestColorList) {
            // Compute Cost of each Vertex if Moved to different color
            int minConflict = Integer.MAX_VALUE;
            for (int c = 1; c <= k; c++) {
                costMatrix[node][c - 1] = computeCost(node, c);

                if (costMatrix[node][c - 1] < minConflict) {
                    bestColorList[node].clear();
                    bestColorList[node].add(c);
                    minConflict = costMatrix[node][c - 1];
                } else if (costMatrix[node][c - 1] == minConflict) {
                    bestColorList[node].add(c);
                }
            }
        }
 
        // Computes Cost of each move
        private int computeCost(int vertex, int color) {
            int cost = 0;
            for (int neighbor : instance.getAdjacent(vertex)) {
                if (coloring[neighbor] == color) {
                    cost++;
                }
            }
            return cost;
        }

        public int getMaxCardinalityClass() {
            int[] counts = new int[this.k];
            int maxCardinality = -1;
            int maxCardinalityClass = -1;
            for (int i = 0; i < instance.getNumNodes(); i++) {
                if (this.coloring[i] > 0) {
                    counts[this.coloring[i] - 1]++;
                    if (counts[this.coloring[i] - 1] > maxCardinality) {
                        maxCardinality = counts[this.coloring[i] - 1];
                        maxCardinalityClass = this.coloring[i];
                    }
                }
            }
            return maxCardinalityClass;
        }

       

    }

}