package papers;

import java.util.*;
import general.Instance;
import general.Move;
import general.HeuristicClasses.*;
import general.SolutionClasses.PartialColoring;
import general.SolutionClasses.Solution;
import general.Options;

public class Blochilger2008Heuristic extends GCPHeuristic {
    // Paper parameter ranges: phi in [500, 5000], alpha in [5, 30], b in [1, 2]
    private int fooFrequencyMin = 500;
    private int fooFrequencyMax = 5000;
    private int fooIncrementMin = 5;
    private int fooIncrementMax = 30;
    private int fooThresholdMin = 1;
    private int fooThresholdMax = 2;

    private int initialK;
    private int maxIterations;
    private String tenureStrategy = "foo";
    private int initialTenure; 

    public Blochilger2008Heuristic(Options options) {
        super(options);
        try {
            maxIterations = Integer.parseInt(options.extras.get("maxiterations"));
            // providing an initial k is optional, defaults to the max chromatic number
            initialK = options.extras.containsKey("initialk")
                    ? Integer.parseInt(options.extras.get("initialk"))
                    : this.instance.getMaxChromatic();

            // Backward-compatible overrides for fixed values.
            if (options.extras.containsKey("frequency")) {
                int frequency = Integer.parseInt(options.extras.get("frequency"));
                fooFrequencyMin = frequency;
                fooFrequencyMax = frequency;
            }
            if (options.extras.containsKey("increment")) {
                int increment = Integer.parseInt(options.extras.get("increment"));
                fooIncrementMin = increment;
                fooIncrementMax = increment;
            }
            if (options.extras.containsKey("threshold")) {
                int threshold = Integer.parseInt(options.extras.get("threshold"));
                fooThresholdMin = threshold;
                fooThresholdMax = threshold;
            }
            if(options.extras.containsKey("initialtenure")) {
                initialTenure = Integer.parseInt(options.extras.get("initialtenure"));
            } else {
                // if not initial tenure is not provided, use the dynamic formula from the paper as the default
                initialTenure = (int) (0.6 * instance.getNumNodes() + Heuristic.random(10));
            }
            if(options.extras.containsKey("tenurestrategy")) {
                tenureStrategy = options.extras.get("tenurestrategy");
                if (!tenureStrategy.equals("foo") && !tenureStrategy.equals("dynamic") && !tenureStrategy.equals("static")) {
                    throw new IllegalArgumentException("Invalid tenure strategy. Must be 'foo', 'dynamic', or 'static'.");
                }
            }

            // Explicit range overrides, if not provided, won't randomize
            if (options.extras.containsKey("frequencymin")) {
                fooFrequencyMin = Math.max(1, Integer.parseInt(options.extras.get("frequencymin")));
            }
            if (options.extras.containsKey("frequencymax")) {
                fooFrequencyMax = Math.max(fooFrequencyMin + 1, Integer.parseInt(options.extras.get("frequencymax")));
            }
            if (options.extras.containsKey("incrementmin")) {
                fooIncrementMin = Math.max(1, Integer.parseInt(options.extras.get("incrementmin")));
            }
            if (options.extras.containsKey("incrementmax")) {
                fooIncrementMax = Math.max(fooIncrementMin + 1, Integer.parseInt(options.extras.get("incrementmax")));
            }
            if (options.extras.containsKey("thresholdmin")) {
                fooThresholdMin = Math.max(1, Integer.parseInt(options.extras.get("thresholdmin")));
            }
            if (options.extras.containsKey("thresholdmax")) {
                fooThresholdMax = Math.max(fooThresholdMin + 1, Integer.parseInt(options.extras.get("thresholdmax")));
            }

            if (fooFrequencyMin > fooFrequencyMax || fooIncrementMin > fooIncrementMax
                    || fooThresholdMin > fooThresholdMax) {
                throw new IllegalArgumentException("Invalid Blochilger2008Heuristic parameter ranges.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Missing or invalid extended parameters for Blochilger2008Heuristic. Required parameter: maxiterations. Optional: initialk, frequency/frequencymin/frequencymax, increment/incrementmin/incrementmax, threshold/thresholdmin/thresholdmax.");
        }

    }

    public void run() {
        int k = this.initialK;
        // if blochilger breaks early and there is still time left
        // do a random restart and rerun the paper's algorithm 
        while (report()) {
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
                    // until you find a valid solution, then stop
                    revisitingK = true;
                    k++;
                }
                // resets the coloring with the new k
                solution = new Blochilger2008Solution(this.getInstance(),
                        Blochilger2008Solution.partialColoring(this, k),
                        k);
            }
        }
    }

    public class Blochilger2008Solution extends PartialColoring {
        // countsMatrix[node][color] = number of adjacent nodes colored with that color        
        protected int[][] countsMatrix; 
        private int bestLocalObjective = Integer.MAX_VALUE;
        private Solution bestLocalSolution;

        public Blochilger2008Solution(Instance instance, int[] coloring, int k) {
            super(instance, coloring, k);
            this.countsMatrix = new int[instance.getNumNodes() + 1][this.k + 1];
            for (int n = 1; n <= instance.getNumNodes(); n++) {
                HashSet<Integer> adj = instance.getAdjacent(n);
                for (int adjv : adj) {
                    if (coloring[n] > 0) {
                        this.countsMatrix[adjv][coloring[n]]++;
                    }
                }
            }
            updateBestSolution(this);
        }

        public class BlochilgerTabuSearch extends TabuSearch<Move> {
            // to track the min and max objective in the last "frequency" moves
            protected int maxObj = Integer.MIN_VALUE;
            protected int minObj = Integer.MAX_VALUE;

            // tabu search variables
            protected int tenure;
            protected int frequency;
            protected int threshold;
            protected int increment;
            protected ArrayList<Move> removedMoves; // used to mark moves tabu

            public BlochilgerTabuSearch() {
                super(Blochilger2008Solution.this, Blochilger2008Heuristic.this);
                this.tenure = Blochilger2008Heuristic.this.initialTenure;
                randomizeFooParameters();
                this.tabuMap = new HashMap<>();
            }

            // parameters can either be fixed or randomized every phi iterations
            private void randomizeFooParameters() {
                this.frequency = Heuristic.randomInRange(fooFrequencyMin, fooFrequencyMax);
                this.increment = Heuristic.randomInRange(fooIncrementMin, fooIncrementMax);
                this.threshold = Heuristic.randomInRange(fooThresholdMin, fooThresholdMax);
            }

            public void updateTenure() {
                switch (tenureStrategy) {
                    case "dynamic":
                        dynTenure();
                        break;
                    case "foo":
                        fooTenure();
                        break;
                    case "static":
                        // do nothing, tenure does not change
                        break;
                }
            }

            public void dynTenure() {
                this.tenure = (int) (0.6 * uncolored.size() + Heuristic.random(10));
            }

            public void fooTenure() {
                if (maxObj - minObj <= threshold) {
                    this.tenure += increment;
                } else {
                    this.tenure = Math.max(0, tenure - 1); // ensure not negative
                }
                maxObj = Integer.MIN_VALUE;
                minObj = Integer.MAX_VALUE; 
                randomizeFooParameters();
            }

            public Move generateBestNeighbor(int iteration) {
                Move bestMove = null;
                ArrayList<Move> tiedBestMoves = new ArrayList<>();

                for (int node : uncolored) {
                    for (int color = 1; color <= k; color++) {
                        Move move = new Move(node, color, solution);
                        if (!isTabu(move, iteration) || move.getObjective() < bestLocalObjective) {
                            if (bestMove == null || move.getObjective() < bestMove.getObjective()) {
                                bestMove = move; // possible break? make copy? shouldn't though
                                tiedBestMoves.clear();
                                tiedBestMoves.add(move);
                            } else if (move.getObjective() == bestMove.getObjective()) {
                                tiedBestMoves.add(move);
                            }
                        }
                    }
                }

                if (!tiedBestMoves.isEmpty()) {
                    return tiedBestMoves.get(Heuristic.random(tiedBestMoves.size()));
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

                updateBestSolution(Blochilger2008Solution.this);
                minObj = Math.min(minObj, getObjective());
                maxObj = Math.max(maxObj, getObjective());
            }

            public void tabuAppend(Move move, int iteration) {
                for (Move m : removedMoves) {
                    tabuMap.put(m, iteration + this.tenure);
                }
                removedMoves.clear();
            }

            public void tabuSearch() {
                int iteration = 1;
                while (uncolored.size() > 0 && iteration <= maxIterations && report()) {
                    Move move = generateBestNeighbor(iteration);

                    if (move == null) {
                        break; // No valid moves available
                    }

                    if ((iteration) % frequency == 0) {
                        updateTenure();
                    }

                    makeMove(move);

                    if(updateBestSolution(Blochilger2008Solution.this)) {
                        iteration = 1;
                    }

                    tabuAppend(move, iteration);

                    iteration++;
                }
            }
        }

        public void calcNeighborObjective(Move move) {
            move.setObjective(getObjective() - 1 + this.countsMatrix[move.getNode()][move.getColor()]);
        }

        public boolean updateBestSolution(PartialColoring solution) {
            if (solution.getObjective() < bestLocalObjective) {
                this.bestLocalObjective = solution.getObjective();
                this.bestLocalSolution = new PartialColoring(solution);
                return true;
            } 
            return false;
        }

        public Solution getBestSolution() {
            return this.bestLocalSolution;
        }

        public void tabuSearch() {
            BlochilgerTabuSearch ts = new BlochilgerTabuSearch();
            ts.tabuSearch();
        }
    }
}
