package papers;

import java.util.HashSet;

import general.*;
import general.HeuristicClasses.*;
import general.SolutionClasses.SolutionConflictCounts;

public class GASkeleton extends GCPHeuristic {
    private GASkeletonSolution[] population;
    private int k;
    private int iteration;
    private double diversityThreshold = 20.0; // Paper uses low-diversity threshold around D < 20

    public GASkeleton(Options options) {
        super(options);
        // First and only extra parameter is population size, parse
        int popSize = Integer.parseInt(options.extras.get("popsize"));
        this.k = instance.getMaxChromatic();
        this.population = new GASkeletonSolution[popSize];
    }

    public void run() {
        GASkeletonSolution solution;
        InitPopulation();
        iteration = 0;
        do {
            iteration++;
            // chooses 2 random parents
            int s1 = GCPHeuristic.random(population.length);
            int s2 = GCPHeuristic.randomNotEqual(population.length, s1);

            solution = instantiateSolution(instance, crossOver(population[s1], population[s2]), this.k);

            // if a valid solution is found, we will restart the algorithm looking for
            // (the lowest k found) - 1 colors
            // this is our own implementation of moving between k's
            if (solution.isValidSolution()) {
                int lowestKFound = calcK(solution);
                solution.setK(lowestKFound);
                this.report(solution);
                this.k = lowestKFound - 1;
                System.out.println("K found: " + lowestKFound);
                InitPopulation();
                iteration = 0;
            } else {
                // updatePopulation
                int toReplace = s1;
                if (population[s1].getObjective() < population[s2].getObjective()) {
                    toReplace = s2;
                }
                population[toReplace] = solution;
            }

            // Track and check diversity every 100 iterations
            if (iteration % 100 == 0) {
                double diversity = calculatePopulationDiversity();
                System.out.println("Iteration " + iteration + ": Diversity = " + String.format("%.2f", diversity));
                if (diversity < diversityThreshold) {
                    System.out.println("Population diversity too low (" + String.format("%.2f", diversity)
                            + "), restarting population");
                    InitPopulation();
                    iteration = 0;
                }
            }
        } while (this.report());
    }

    public void InitPopulation() {
        int[] deterministicColoring = greedyConstructionNonConflicted(this, this.k);
        for (int i = 0; i < population.length; i++) {
            population[i] = instantiateSolution(instance,
                    fillConflicts(deterministicColoring, this.k), this.k);
        }
    }

    public int[] crossOver(GASkeletonSolution s1, GASkeletonSolution s2) {
        int[] coloring = new int[instance.getNumNodes() + 1];
        int[] c1 = new int[instance.getNumNodes() + 1];
        int[] c2 = new int[instance.getNumNodes() + 1];

        // making copies of the colorings
        for (int i = 1; i <= instance.getNumNodes(); i++) {
            c1[i] = s1.getColoring()[i];
            c2[i] = s2.getColoring()[i];
        }

        for (int l = 1; l <= k; l++) {
            if (l % 2 == 1) {
                int color = getMaxCardinalityClass(c1);
                for (int i = 1; i <= instance.getNumNodes(); i++) {
                    if (c1[i] == color) {
                        c1[i] = -1;
                        c2[i] = -1;
                        coloring[i] = l;
                    }
                }
            } else {
                int color = getMaxCardinalityClass(c2);
                for (int i = 1; i <= instance.getNumNodes(); i++) {
                    if (c2[i] == color) {
                        c1[i] = -1;
                        c2[i] = -1;
                        coloring[i] = l;
                    }
                }
            }
        }

        // if there are leftover nodes, just randomly assign them
        for (int i = 1; i <= instance.getNumNodes(); i++) {
            if (coloring[i] <= 0) {
                coloring[i] = GCPHeuristic.random(this.k) + 1;
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
        for (int i = 1; i <= instance.getNumNodes(); i++) {
            int c = solution.getColoring()[i];
            // Unique Color
            if (!visited[c]) {
                visited[c] = true;
                count++;
            }
        }

        return count;
    }

    /**
     * Calculates the partition distance between two colorings.
     * This is the minimum number of elementary transformations (moving one node
     * from one color class to another)
     * needed to transform coloring1 into coloring2.
     *
     * Important: direct label mismatch is not a valid partition distance for graph
     * coloring because color IDs are arbitrary. Two equivalent partitions may use
     * different color labels (a permutation), which can make direct mismatch look
     * large even when structures are identical. We therefore match color classes
     * first (max overlap assignment) and then compute the remaining node moves.
     */
    public int calculatePartitionDistance(int[] coloring1, int[] coloring2) {
        int numNodes = instance.getNumNodes();

        // For very large k, avoid cubic assignment cost and fall back to direct mismatch.
        if (k > 200) {
            int mismatch = 0;
            for (int i = 1; i <= numNodes; i++) {
                if (coloring1[i] != coloring2[i]) {
                    mismatch++;
                }
            }
            return mismatch;
        }

        int[][] overlap = new int[k][k];
        for (int i = 1; i <= numNodes; i++) {
            int c1 = coloring1[i] - 1;
            int c2 = coloring2[i] - 1;
            // if c1 and c2 are valid colors 
            if (c1 >= 0 && c1 < k && c2 >= 0 && c2 < k) {
                overlap[c1][c2]++; // increment the overlap count for that pair of color classes
            }
        }

        int maxOverlap = maxWeightAssignment(overlap);
        return numNodes - maxOverlap;
    }

    // Hungarian algorithm (maximization form) over a square matrix.
    private int maxWeightAssignment(int[][] weights) {
        int n = weights.length;
        if (n == 0) {
            return 0;
        }

        // Convert maximization to minimization by subtracting every weight from maxW.
        int maxW = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (weights[i][j] > maxW) {
                    maxW = weights[i][j];
                }
            }
        }

        int[] rowPotential = new int[n + 1];
        int[] colPotential = new int[n + 1];
        int[] matchedRowForColumn = new int[n + 1];
        int[] predecessorColumn = new int[n + 1];

        for (int rowIndex = 1; rowIndex <= n; rowIndex++) {
            matchedRowForColumn[0] = rowIndex; // mapping of col(index) to row(value)
            int currCol = 0;
            int[] colReduce = new int[n + 1];
            boolean[] usedCols = new boolean[n + 1];
            for (int colInd = 1; colInd <= n; colInd++) {
                colReduce[colInd] = Integer.MAX_VALUE;
                usedCols[colInd] = false;
            }


            do {
                usedCols[currCol] = true;
                int currRow = matchedRowForColumn[currCol];
                int minDelta = Integer.MAX_VALUE; // min in colReduced
                int nextColumn = 0;

                // reduction for columns
                for (int colInd = 1; colInd <= n; colInd++) {
                    if (!usedCols[colInd]) {
                        // to make maximization problem into minimization, we subtract weights from maxW
                        int transformedCost = maxW - weights[currRow - 1][colInd - 1];
                        int reducedCost = transformedCost - rowPotential[currRow] - colPotential[colInd];
                        if (reducedCost < colReduce[colInd]) {
                            colReduce[colInd] = reducedCost;
                            predecessorColumn[colInd] = currCol;
                        }
                        if (colReduce[colInd] < minDelta) {
                            minDelta = colReduce[colInd];
                            nextColumn = colInd;
                        }
                    }
                }

                for (int colInd = 0; colInd <= n; colInd++) {
                    if (usedCols[colInd]) {
                        rowPotential[matchedRowForColumn[colInd]] += minDelta;
                        colPotential[colInd] -= minDelta;
                    } else {
                        colReduce[colInd] -= minDelta;
                    }
                }
                currCol = nextColumn;
            } while (matchedRowForColumn[currCol] != 0);

            do {
                int previousColumn = predecessorColumn[currCol];
                matchedRowForColumn[currCol] = matchedRowForColumn[previousColumn];
                currCol = previousColumn;
            } while (currCol != 0);
        }

        int total = 0;
        for (int j = 1; j <= n; j++) {
            int i = matchedRowForColumn[j];
            if (i > 0) {
                total += weights[i - 1][j - 1];
            }
        }
        return total;
    }

    /**
     * Calculates the average partition distance between all pairs of solutions in
     * the population.
     * This measures the diversity of the population.
     */
    public double calculatePopulationDiversity() {
        int popSize = population.length;
        if (popSize <= 1)
            return 0.0;

        double totalDistance = 0.0;
        int pairs = 0;

        for (int i = 0; i < popSize; i++) {
            for (int j = i + 1; j < popSize; j++) {
                int distance = calculatePartitionDistance(population[i].getColoring(), population[j].getColoring());
                totalDistance += distance;
                pairs++;
            }
        }

        return pairs > 0 ? totalDistance / pairs : 0.0;
    }

    // returns the color partition with the most nodes
    public int getMaxCardinalityClass(int[] coloring) {
        int[] counts = new int[this.k + 1];
        int maxCardinality = -1;
        int maxCardinalityClass = 0;
        for (int i = 1; i <= instance.getNumNodes(); i++) {
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

    // this method is meant to be overriden, so that subclass heuristics will have
    // their own subclass solution class constructors called
    public GASkeletonSolution instantiateSolution(Instance instance, int[] coloring, int colors)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("instantiateSolution not implemented in GASkeleton base class");
    }

    public class GASkeletonSolution extends SolutionConflictCounts {
        public GASkeletonSolution(Instance instance, int[] coloring, int colors) {
            super(instance, coloring, colors);
        }

        public GASkeletonSolution(GASkeletonSolution other) {
            super(other);
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
        HashSet<Integer>[] satDegree = new HashSet[instance.getNumNodes() + 1];
        int[] coloring = new int[instance.getNumNodes() + 1];

        // saturation degree refers to the number of different color class in adjacent
        // nodes
        // satDegree[i] is the set of colors that are assigned to adjacent nodes to node
        // i
        for (int i = 1; i <= instance.getNumNodes(); i++) {
            satDegree[i] = new HashSet<Integer>();
        }

        for (int i = 1; i <= instance.getNumNodes(); i++) {
            int minAllowed = Integer.MAX_VALUE;
            int currNode = -1;

            // loops through all nodes and searches for the node with the minimum colors
            // allowed without giving penalty
            for (int node = 1; node <= instance.getNumNodes(); node++) {
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
    public static int[] fillConflicts(int[] coloring, int k) {
        // randomly colors any nodes that couldn't be colored without penalty
        // (aka where this.coloring[i] == -1)
        for (int i = 1; i < coloring.length; i++) {
            if (coloring[i] == -1) {
                coloring[i] = GCPHeuristic.random(k) + 1; // colors are 1 indexed
            }
        }

        return coloring;
    }
}
