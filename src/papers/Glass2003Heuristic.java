package papers;

import general.Instance;
import general.HeuristicClasses.GCPHeuristic;
import general.SolutionClasses.SolutionConflictCounts;
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
            int[][] costMatrix = new int[numNodes][k];

            //Best color List Structure for each vertex
            ArrayList<Integer>[] bestColorList = new ArrayList[numNodes];

            int minConflict = Integer.MAX_VALUE;

            // Initialize cost matrix and best color list
            for (int i = 0; i < numNodes; i++) {
                bestColorList[i] = new ArrayList<Integer>();
                
                //Compute Cost of each Vertex if Moved to different color
                for (int c = 1; c <= k; c++) {
                    costMatrix[i][c-1] = computeCost(i, c);

                    if (costMatrix[i][c-1] < minConflict){
                        bestColorList[i].clear();
                        bestColorList[i].add(c);
                        minConflict =  costMatrix[i][c-1];
                    }
                    else if (costMatrix[i][c-1] == minConflict){
                        bestColorList[i].add(c);
                    }
                }

            }

            boolean changed = true;

            // Cycles until no changes can be made
            while (changed && report()) {

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
                            for (int c = 1; c <= k; c++) {
                                costMatrix[neighbor][c-1] = computeCost(neighbor, c);

                                if (costMatrix[neighbor][c-1] < minConflict){
                                    bestColorList[neighbor].clear();
                                    bestColorList[neighbor].add(c);
                                    minConflict =  costMatrix[neighbor][c-1];

                                }
                                else if (costMatrix[neighbor][c-1] == minConflict){
                                    bestColorList[neighbor].add(c);
                                }   
                            }
                        }

                        
                        // Update own row too
                        for (int c = 1; c <= k; c++) {
                            costMatrix[i][c-1] = computeCost(i, c);

                            if (costMatrix[i][c-1] < minConflict){
                                    bestColorList[i].clear();
                                    bestColorList[i].add(c);
                                    minConflict =  costMatrix[i][c-1];

                                }
                            else if (costMatrix[i][c-1] == minConflict){
                                bestColorList[i].add(c);
                            }   
                        }
                    }
                }
            }

            // Finally update the objective and conflict counters
            // calcObjective();
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
            for (int i = 0; i < instance.getNumNodes(); i++) {
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