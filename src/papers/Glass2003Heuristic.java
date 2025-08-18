package papers;

import general.Heuristic;
import general.Instance;

import java.util.HashSet;
import java.util.*;

public class Glass2003Heuristic extends Heuristic {
    private Glass2003Solution[] population;
    private int k;

    public Glass2003Heuristic(Instance instance, double runtime, int popSize) {
        super(instance, runtime);
        k = instance.getMaxChromatic();
        System.out.println("Max Chromatic : " + k);

        Glass2003Solution solution;
        System.out.println("Population About to be Initialized");
        InitPopulation(popSize);
        System.out.println("Population Initialized");
        int[] coloringprint;

        for (int i = 0; i < population.length; i++) {
            coloringprint = population[i].coloring;
            System.out.print("Coloring " + i + ": ");
            for (int u = 0; u < coloringprint.length; u++) {
                System.out.print(coloringprint[u]);
            }
            System.out.println("");
        }

        do {
            // chooses 2 random parents
            int s1 = random(population.length);
            int s2 = random(population.length);

            while (s2 == s1) {
                s2 = random(population.length);
            }

            solution = new Glass2003Solution(this, crossOver(population[s1], population[s2]), this.k);
            /*
             * Creating a solution object calls calc objective and vertex desent on it
             */

            // if a valid solution is found, we will restart the algorithm looking for k - 1
            // colors
            if (solution.objective == 0) {
                //Check the achieved k **THIS IS SO THAT IF HEURISTIC OUTPERFORMS K LIMIT GIVEN, WE CAN SKIP K COLORINGS
                int newK = calcK(solution);
                this.k = newK-1;
                solution.k = newK;
                InitPopulation(popSize);
            } else {
                // updatePopulation
                int toReplace = s1;
                if (population[s1].getObjective() < population[s2].getObjective()) {
                    toReplace = s2;
                }
                population[toReplace] = solution;
            }

        } while (report(solution));
    }

    public void InitPopulation(int popSize) {
        this.population = new Glass2003Solution[popSize];
        for (int i = 0; i < popSize; i++) {

            if (report()) {
                population[i] = new Glass2003Solution(this,
                        Glass2003Solution.greedyConstruction(getInstance(), this.k, this), k);
            } // else break?
        }
    }

    public int[] crossOver(Glass2003Solution s1, Glass2003Solution s2) {

        int[] combined = new int[instance.getNumNodes()];

        for (int l = 1; l <= k; l++) {
            if (l % 2 == 1) {
                int color = s1.getMaxCardinalityClass();
                for (int i = 0; i < instance.getNumNodes(); i++) {
                    if (s1.coloring[i] == color) {
                        s1.coloring[i] = -1;
                        s2.coloring[i] = -1;
                        combined[i] = l;
                    }
                }
            } else {
                int color = s2.getMaxCardinalityClass();
                for (int i = 0; i < instance.getNumNodes(); i++) {
                    if (s2.coloring[i] == color) {
                        s1.coloring[i] = -1;
                        s2.coloring[i] = -1;
                        combined[i] = l;
                    }
                }
            }
        }

        // if there are leftover nodes, just randomly assign them
        for (int i = 0; i < instance.getNumNodes(); i++) {
            if (combined[i] <= 0) {

                // Changed this to this.random(k) + 1
                combined[i] = random(k) + 1;
            }
        }

        return combined;
    }

    public int calcK(Glass2003Solution sol){
        boolean[] visited = new boolean [this.k+1];

        int count = 0;
        for (int c : sol.coloring){
            //Unique Color
            if (!visited[c]){
                visited[c] = true;
                count++;
            }
        }

        return count;

    }

    public class Glass2003Solution extends SolutionConflictCounts{

        public Glass2003Solution(Heuristic heuristic, int[] coloring, int colors) {
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
                        int newColor = bestColorList[i].get(Heuristic.random(bestColorList[i].size()));
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