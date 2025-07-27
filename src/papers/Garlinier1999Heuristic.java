package papers;

import java.util.HashSet;
import java.util.HashMap;

import general.Heuristic;
import general.Instance;

public class Garlinier1999Heuristic extends Heuristic {
    private Garlinier1999Solution[] population;
    private int k;

    public Garlinier1999Heuristic(Instance instance, double runtime, int popSize) {
        super(instance, runtime);
        k = instance.getMaxChromatic();
        Garlinier1999Solution solution;
        InitPopulation(popSize);
        do {
            // chooses 2 random parents
            int s1 = this.random(population.length);
            int s2 = this.random(population.length);
            while (s2 == s1) {
                s2 = this.random(population.length);
            }

            solution = crossOver(population[s1], population[s2]);
            solution.localSearch(1000, 10, 4, 3);

            // if a valid solution is found, we will restart the algorithm looking for k - 1
            // colors
            if (solution.objective == 0) {
                solution.reduceK();
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
        this.population = new Garlinier1999Solution[popSize];
        for (int i = 0; i < popSize; i++) {
            population[i] = new Garlinier1999Solution(this, k);
            population[i].greedyConstruction();
        }
    }

    public Garlinier1999Solution crossOver(Garlinier1999Solution s1, Garlinier1999Solution s2) {
        Garlinier1999Solution combined = new Garlinier1999Solution(this, this.k);
        for (int l = 1; l <= k; l++) {
            if (l % 2 == 1) {
                s1.calcMaxCardinalityClass();
                int color = s1.maxCardinalityClass;
                for (int i = 0; i < instance.getNumNodes(); i++) {
                    if (s1.coloring[i] == color) {
                        s1.coloring[i] = -1;
                        s2.coloring[i] = -1;
                        combined.coloring[i] = l;
                    }
                }
            } else {
                s2.calcMaxCardinalityClass();
                int color = s2.maxCardinalityClass;
                for (int i = 0; i < instance.getNumNodes(); i++) {
                    if (s2.coloring[i] == color) {
                        s1.coloring[i] = -1;
                        s2.coloring[i] = -1;
                        combined.coloring[i] = l;
                    }
                }
            }
        }

        // if there are leftover nodes, just randomly assign them
        for (int i = 0; i < instance.getNumNodes(); i++) {
            if (combined.coloring[i] <= 0) {
                combined.coloring[i] = this.random(instance.getNumNodes());
            }
        }

        return combined;

    }

    public class Garlinier1999Solution extends SolutionConflictObjective {
        private Heuristic heuristic;
        private int maxCardinalityClass = -1;
        private int nb_cfl;
        private double[] A;

        public Garlinier1999Solution(Heuristic heuristic, int colors) {
            this.heuristic = heuristic;
            this.instance = heuristic.getInstance();
            this.k = colors;
            this.objective = 0;
            this.coloring = new int[instance.getNumNodes()];
            A = new double[instance.getNumEdges() + 1];
            for (int i = 0; i < A.length; i++) {
                A[i] = i - 1;
            }
        }

        public class GarlinierTabuSearch extends TabuSearch {
            private int a;
            private int alpha;

            public GarlinierTabuSearch(int a, int alpha) {
                tabuMap = new HashMap<>();
                this.a = a;
                this.alpha = alpha;
            }

            public void updateTenure() {
                tenure = heuristic.random(a) + alpha * nb_cfl;
            }
        }

        private void greedyConstruction() {
            // Initialization
            HashSet<Integer>[] satDegree = new HashSet[instance.getNumNodes()];
            for (int i = 0; i < instance.getNumNodes(); i++) {
                satDegree[i] = new HashSet<Integer>();
            }

            for (int i = 0; i < instance.getNumNodes(); i++) {
                int minAllowed = Integer.MAX_VALUE;
                int currNode = -1;
                boolean[] usedColors = new boolean[this.k + 1];

                // loops through all nodes and searchs for the node with the minimum colors
                // allowed without giving penalty
                for (int node = 0; node < instance.getNumNodes(); node++) {
                    if (this.coloring[node] == 0) { // Note: 0 means unvisited
                        int allowed = this.k - satDegree[node].size();
                        // -1 will notate that there is no allowed color
                        // these nodes will be randomly colored later
                        if (allowed == 0) {
                            this.coloring[node] = -1;
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

                // fills up an array denoted which colors are used by the current nodes
                // neighbors
                for (int neighbor : instance.getAdjacent(currNode)) {
                    if (this.coloring[neighbor] > 0) {
                        usedColors[this.coloring[neighbor]] = true;
                    }
                }

                // finds the smallest color class that is not used by the current nodes
                // neighbors
                int minColor = -1;
                for (int j = 1; j < usedColors.length; j++) {
                    if (!usedColors[j]) {
                        minColor = j;
                        break;
                    }
                }

                // currNode is assigned minColor, then updates the sat degree of all its
                // neighbors by adding the minColor to the set of colors neighboring the
                // neighbor
                this.coloring[currNode] = minColor;
                for (int neighbor : instance.getAdjacent(currNode)) {
                    satDegree[neighbor].add(minColor);
                }
            }

            // randomly colors any nodes that couldn't be colored without penalty
            // (aka where this.coloring[i] == -1)
            for (int i = 0; i < instance.getNumNodes(); i++) {
                if (this.coloring[i] == -1) {
                    this.coloring[i] = heuristic.random(this.k) + 1;
                    this.objective++;
                }
            }

            localSearch(2000, 10, 4, 3);
            calcObjective();
        }

        // this is different than the normal calc objective because it also updates
        // nb_cfl which will be used to
        // determine the length of the tabu tenure at each iteration
        public void calcObjective() {
            int obj = 0;
            for (int i = 0; i < coloring.length; i++) {
                HashSet<Integer> adj = this.instance.getAdjacent(i);
                boolean conflictedNode = false;
                for (int adjv : adj) {
                    // If i < adjv, that edge hasn't been checked yet, this prevents from double
                    // counting
                    if (coloring[i] == coloring[adjv]) {
                        if (i < adjv) {
                            obj += 1;
                        }
                        if (!conflictedNode) {
                            nb_cfl++;
                            conflictedNode = true;
                        }
                    }
                }
            }
            objective = obj;
            validSolution = objective == 0;
        }

        public void localSearch(int iterations, int rep, int A, int alpha) {
            // read more about tabu tenure and rep count
            GarlinierTabuSearch ts = new GarlinierTabuSearch(A, alpha);
            int iteration = 0;
            while (objective > 0 && iteration < iterations && heuristic.report()) {
                Move neighbor = ts.generateBestRepNeighbor(rep, iteration);
                Move tabuMove = new Move(neighbor.node, this.coloring[neighbor.node], this);
                ts.updateTenure();
                ts.tabuMap.put(tabuMove, iteration + ts.tenure); // a move is still tabu as long as the iteration is <=
                                                                 // curr iteration + tabuTenure
                makeMove(neighbor);
                iteration++;
            }

        }

        public void calcMaxCardinalityClass() {
            int[] counts = new int[this.k];
            int maxCardinality = -1;
            for (int i = 0; i < instance.getNumNodes(); i++) {
                if (this.coloring[i] > 0) {
                    counts[this.coloring[i]]++;
                    if (counts[this.coloring[i]] > maxCardinality) {
                        maxCardinality = counts[this.coloring[i]];
                        maxCardinalityClass = this.coloring[i];
                    }
                }
            }
        }

    }
}
