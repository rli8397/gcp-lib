package papers;

import general.HeuristicClasses.*;
import general.*;

/*
    Notes: Objective is the number of edges having both endpoints in the same class
    
 */
public class Garlinier1999Heuristic extends GASkeleton {
    private int a;
    private double alpha;
    private int rep;
    private int maxIterations;
    private double diversityThreshold = 20.0;
    private int diversityCheckInterval = 100;

    public Garlinier1999Heuristic(Options options) {
        super(options);
        try {
            this.a = Integer.parseInt(get_cmdline_arg("a"));
            this.alpha = Double.parseDouble(get_cmdline_arg("alpha"));
            this.rep = Integer.parseInt(get_cmdline_arg("rep"));
            this.maxIterations = Integer.parseInt(get_cmdline_arg("maxiterations"));
            if (options.extras.containsKey("diversitythreshold")) {
                this.diversityThreshold = Double.parseDouble(get_cmdline_arg("diversitythreshold"));
            }
            if (options.extras.containsKey("diversitycheckinterval")) {
                this.diversityCheckInterval = Integer.parseInt(get_cmdline_arg("diversitycheckinterval"));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Missing or invalid extended parameters for Garlinier1999Heuristic. Required parameters: a, alpha, rep, maxiterations. Optional parameters: diversitythreshold, diversitycheckinterval.");
        }
    }

    @Override
    protected boolean shouldRestartPopulation(int iteration) {
        if (iteration % diversityCheckInterval != 0) {
            return false;
        }

        double diversity = calculatePopulationDiversity();
        if (verbosity >= 3) {
            System.out.println("Iteration " + iteration + ": Diversity = " + String.format("%.2f", diversity));
        }
        if (diversity < diversityThreshold) {
            if (verbosity >= 3) {
                System.out.println("Population diversity too low (" + String.format("%.2f", diversity)
                        + "), restarting population");
            }
            return true;
        }
        return false;
    }

    /**
     * Calculates the partition distance between two colorings.
     * This is the minimum number of node moves needed to transform one partition into another.
     */
    private int calculatePartitionDistance(int[] coloring1, int[] coloring2) {
        int numNodes = instance.getNumNodes();

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
            if (c1 >= 0 && c1 < k && c2 >= 0 && c2 < k) {
                overlap[c1][c2]++;
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
            matchedRowForColumn[0] = rowIndex;
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
                int minDelta = Integer.MAX_VALUE;
                int nextColumn = 0;

                for (int colInd = 1; colInd <= n; colInd++) {
                    if (!usedCols[colInd]) {
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

    private double calculatePopulationDiversity() {
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

    public GASkeletonSolution instantiateSolution(Instance instance, int[] coloring, int k) {
        Garlinier1999Solution sol = new Garlinier1999Solution(instance, coloring, k);
        sol.localSearch();
        return sol.bestSolution;
    }

    public class Garlinier1999Solution extends GASkeleton.GASkeletonSolution {
        private Garlinier1999Solution bestSolution;

        // instantiation will leave solution floating
        // would instantiate either with a greedy or a crossover, like factory
        public Garlinier1999Solution(Instance instance, int[] coloring, int k) {
            super(instance, coloring, k);
        }

        public Garlinier1999Solution(Garlinier1999Solution other) {
            super(other);
        }

        public class GarlinierTabuSearch extends HertzTabuSearch {

            public GarlinierTabuSearch(Garlinier1999Solution solution) {
                super(solution, Garlinier1999Heuristic.this);
                bestSolution = new Garlinier1999Solution(solution);
            }

            public int getTenure() {
                return (int) (GCPHeuristic.random(a) + alpha * nb_cfl);
            }

            public Move generateBestNeighbor(int iteration) {
                Move bestMove = generateBestRepNeighbor(iteration, rep);
                // in order to keep track of the best solution found overall
                if (bestMove != null && bestMove.getObjective() <= bestSolution.getObjective()) {
                    bestSolution = new Garlinier1999Solution(Garlinier1999Solution.this);
                    bestSolution.makeMove(bestMove);
                }
                return bestMove;
            }

            public boolean stopCondition(int iteration) {
                return solution.isValidSolution() || iteration >= maxIterations || !heuristic.report();
            }
        }

        public void localSearch() {
            GarlinierTabuSearch ts = new GarlinierTabuSearch(this);
            ts.tabuSearch();
        }

    }
}
