package papers;

import java.util.HashSet;

import general.Instance;
import general.HeuristicClasses.*;
import general.SolutionClasses.SolutionConflictObjective;

public class GASkeleton extends GCPHeuristic {
    private GASkeletonSolution[] population;
    private int k;

    public GASkeleton(Instance instance, double runtime_limit, int popSize) {
            super(instance, runtime_limit);
            GASkeletonSolution solution;
            this.k = instance.getMaxChromatic();
            this.population = new GASkeletonSolution[popSize];
            InitPopulation(population.length);
            do {
                // chooses 2 random parents
                int s1 = GCPHeuristic.random(population.length);
                int s2 = GCPHeuristic.randomNotEqual(population.length, s1);

                solution = new GASkeletonSolution(this, crossOver(population[s1], population[s2]), this.k);

                // if a valid solution is found, we will restart the algorithm looking for k - 1
                // colors
                if (solution.isValidSolution()) {
                    System.out.println(k);
                    this.report(solution, k);
                    int newK = calcK(solution);
                    this.k = newK - 1;
                    InitPopulation(population.length);
                } else {
                    // updatePopulation
                    int toReplace = s1;
                    if (population[s1].getObjective() < population[s2].getObjective()) {
                        toReplace = s2;
                    }
                    population[toReplace] = solution;
                }

            } while (this.report());
        }

    public void InitPopulation(int popSize) {
        int[] deterministicColoring = greedyConstructionNonConflicted(this, this.k);
        for (int i = 0; i < popSize; i++) {
            population[i] = new GASkeletonSolution(this,
                    fillConflicts(this, deterministicColoring, this.k), this.k);
        }
    }

    public int[] crossOver(GASkeletonSolution s1, GASkeletonSolution s2) {
        int[] coloring = new int[instance.getNumNodes()];
        int[] c1 = new int[instance.getNumNodes()];
        int[] c2 = new int[instance.getNumNodes()];

        for (int i = 0; i < instance.getNumNodes(); i++) {
            c1[i] = s1.getColoring()[i];
            c2[i] = s2.getColoring()[i];
        }

        for (int l = 1; l <= k; l++) {
            if (l % 2 == 1) {
                int color = getMaxCardinalityClass(c1);
                for (int i = 0; i < coloring.length; i++) {
                    if (c1[i] == color) {
                        c1[i] = -1;
                        c2[i] = -1;
                        coloring[i] = l;
                    }
                }
            } else {
                int color = getMaxCardinalityClass(c2);
                for (int i = 0; i < coloring.length; i++) {
                    if (c2[i] == color) {
                        c1[i] = -1;
                        c2[i] = -1;
                        coloring[i] = l;
                    }
                }
            }
        }

        // if there are leftover nodes, just randomly assign them
        for (int i = 0; i < coloring.length; i++) {
            if (coloring[i] <= 0) {
                coloring[i] = random(this.k) + 1;
            }
        }

        return coloring;
    }

    // calculates how many colors are used ina solution, this is used if a solution
    // is found with
    // a lower k than the target k
    public int calcK(GASkeletonSolution solution) {
        boolean[] visited = new boolean[this.k + 1];

        int count = 0;
        for (int c : solution.getColoring()) {
            // Unique Color
            if (!visited[c]) {
                visited[c] = true;
                count++;
            }
        }

        return count;
    }

    // returns the color partition with the most nodes
    public int getMaxCardinalityClass(int[] coloring) {
        int[] counts = new int[this.k + 1];
        int maxCardinality = -1;
        int maxCardinalityClass = 0;
        for (int i = 0; i < coloring.length; i++) {
            if (coloring[i] > 0) {
                counts[coloring[i]]++;
                if (counts[coloring[i]] > maxCardinality) {
                    maxCardinality = counts[coloring[i]];
                    maxCardinalityClass = coloring[i];
                }
            }
        }
        return maxCardinalityClass;
    }

    public class GASkeletonSolution extends SolutionConflictObjective {
        public GASkeletonSolution(GCPHeuristic heuristic, int[] coloring, int colors) {
            super(heuristic, coloring, colors);
            localSearch();
        }

        public void localSearch() {
        }
    }

    /*
     * At each step of the construction, chose a vertex v that has the minimal
     * number of allowed colors
     * that can be assigned without creating a conflict. We assign it to an allowed
     * color class with the smallest index.
     * This process will create a partial non-conflicted solution.
     */
    protected static int[] greedyConstructionNonConflicted(GCPHeuristic heuristic, int k) {
        // Initialization
        Instance instance = heuristic.getInstance();
        HashSet<Integer>[] satDegree = new HashSet[instance.getNumNodes()];
        int[] coloring = new int[instance.getNumNodes()];

        // saturation degree refers to the number of different color class in adjacent
        // nodes
        // satDegree[i] is the set of colors that are assigned to adjacent nodes to node
        // i
        for (int i = 0; i < instance.getNumNodes(); i++) {
            satDegree[i] = new HashSet<Integer>();
        }

        for (int i = 0; i < instance.getNumNodes(); i++) {
            int minAllowed = Integer.MAX_VALUE;
            int currNode = -1;

            // loops through all nodes and searchs for the node with the minimum colors
            // allowed without giving penalty
            for (int node = 0; node < instance.getNumNodes(); node++) {
                if (coloring[node] == 0) { // Note: 0 means unvisited
                    int allowed = k - satDegree[node].size();
                    // -1 will notate that there is no allowed color
                    // these nodes will be randomly colored later
                    if (allowed == 0) {
                        coloring[node] = -1;
                    } else if (allowed < minAllowed) {
                        minAllowed = allowed;
                        currNode = node;
                    }
                }
            }

            // if no new node is able to be colored without penalty, break
            if (currNode == -1) {
                break;
            }

            // iterates through all colors and finds the smallest one not in adjacent nodes
            int minColor = -1;
            for (int j = 1; j <= k; j++) {
                if (!satDegree[currNode].contains(j)) {
                    minColor = j;
                    break;
                }
            }

            // currNode is assigned minColor, then updates the sat degree of all its
            // neighbors by adding the minColor to their satDegree set (assuming a valid min
            // color is found)
            coloring[currNode] = minColor;
            if (minColor != -1) {
                for (int neighbor : instance.getAdjacent(currNode)) {
                    satDegree[neighbor].add(minColor);
                }
            }

        }

        return coloring;
    }

    // Since we denote in the greedyConstructionNonConflicted coloring[i] = -1
    // represents
    // uncolored nodes, we will iterate through coloring and randomly assign colors
    // to any
    // nodes where coloring[i] = -1
    public static int[] fillConflicts(GCPHeuristic heuristic, int[] coloring, int k) {
        Instance instance = heuristic.getInstance();

        // randomly colors any nodes that couldn't be colored without penalty
        // (aka where this.coloring[i] == -1)
        for (int i = 0; i < instance.getNumNodes(); i++) {
            if (coloring[i] == -1) {
                coloring[i] = GCPHeuristic.random(k) + 1;
            }
        }

        return coloring;
    }
}
