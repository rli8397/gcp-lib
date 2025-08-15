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
        System.out.println("Running heuristic");
        Garlinier1999Solution solution;
        k = instance.getMaxChromatic();
        InitPopulation(popSize);
        do {
            // chooses 2 random parents
            int s1 = random(population.length);
            int s2 = random(population.length);
            while (s2 == s1) {
                s2 = random(population.length);
            }

            solution = new Garlinier1999Solution(this, crossOver(population[s1], population[s2]), this.k);

            // if a valid solution is found, we will restart the algorithm looking for k - 1
            // colors
            if (solution.objective == 0) {
                this.k--;
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
            population[i] = new Garlinier1999Solution(this, Garlinier1999Solution.greedyConstruction(this.instance, this.k, this), this.k);
        }
    }

    public int[] crossOver(Garlinier1999Solution s1, Garlinier1999Solution s2) {
        int[] coloring = new int[instance.getNumNodes()];
        for (int l = 1; l <= k; l++) {
            if (l % 2 == 1) {
                int color = s1.getMaxCardinalityClass();
                for (int i = 0; i < coloring.length; i++) {
                    if (s1.coloring[i] == color) {
                        s1.coloring[i] = -1;
                        s2.coloring[i] = -1;
                        coloring[i] = l;
                    }
                }
            } else {
                int color = s2.getMaxCardinalityClass();
                for (int i = 0; i < coloring.length; i++) {
                    if (s2.coloring[i] == color) {
                        s1.coloring[i] = -1;
                        s2.coloring[i] = -1;
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

    // public Garlinier1999Solution greedyConstruction() {
    //     // Initialization
    //     int[] coloring = new int[instance.getNumNodes()];
    //     int maxColor = Integer.MAX_VALUE;
    //     HashSet<Integer>[] satDegree = new HashSet[instance.getNumNodes()];
    //     for (int i = 0; i < satDegree.length; i++) {
    //         satDegree[i] = new HashSet<Integer>();
    //     }

    //     for (int i = 0; i < coloring.length; i++) {
    //         int minAllowed = Integer.MAX_VALUE;
    //         int currNode = -1;
    //         boolean[] usedColors = new boolean[this.k + 1];

    //         // loops through all nodes and searchs for the node with the minimum colors
    //         // allowed without giving penalty
    //         for (int node = 0; node < satDegree.length; node++) {
    //             if (coloring[node] == 0) { // Note: 0 means unvisited
    //                 int allowed = this.k - satDegree[node].size();
    //                 // -1 will notate that there is no allowed color
    //                 // these nodes will be randomly colored later
    //                 if (allowed == 0) {
    //                     coloring[node] = -1;
    //                 } else if (allowed < minAllowed) {
    //                     minAllowed = allowed;
    //                     currNode = node;
    //                 }
    //             }
    //         }

    //         // if no new node is able to be colored without penalty, break
    //         if (currNode == -1) {
    //             break;
    //         }

    //         // fills up an array denoted which colors are used by the current nodes
    //         // neighbors
    //         for (int neighbor : instance.getAdjacent(currNode)) {
    //             if (coloring[neighbor] > 0) {
    //                 usedColors[coloring[neighbor]] = true;
    //             }
    //         }

    //         // finds the smallest color class that is not used by the current nodes
    //         // neighbors
    //         int minColor = -1;
    //         for (int j = 1; j < usedColors.length; j++) {
    //             if (!usedColors[j]) {
    //                 minColor = j;
    //                 break;
    //             }
    //         }

    //         // currNode is assigned minColor, then updates the sat degree of all its
    //         // neighbors by adding the minColor to the set of colors neighboring the
    //         // neighbor
    //         coloring[currNode] = minColor;
    //         maxColor = Math.max(minColor, maxColor);
    //         for (int neighbor : instance.getAdjacent(currNode)) {
    //             satDegree[neighbor].add(minColor);
    //         }
    //     }

    //     // randomly colors any nodes that couldn't be colored without penalty
    //     // (aka where this.coloring[i] == -1)
    //     for (int i = 0; i < coloring.length; i++) {
    //         if (coloring[i] == -1) {
    //             coloring[i] = this.random(this.k) + 1;
    //             maxColor = Math.max(coloring[i], maxColor);
    //         }
    //     }
    //     Garlinier1999Solution solution = new Garlinier1999Solution(this, coloring, this.k);
    //     solution.localSearch(1000, 5, 4, 3);
    //     solution.calcObjective();
    //     return solution;
    // }

    public class Garlinier1999Solution extends SolutionConflictCounts {
        private int nb_cfl;

        // instantiation will leave solution floating
        // would instantiate either with a greedy or a crossover, like factory
        public Garlinier1999Solution(Heuristic heuristic, int[] coloring, int colors) {
            super(heuristic, coloring, colors);
            localSearch(1000, 5, 4, 3);
        }

        protected static int[] greedyConstruction(Instance instance, int k, Heuristic heuristic) {
            // Initialization
            HashSet<Integer>[] satDegree = new HashSet[instance.getNumNodes()];
            int[] coloring = new int[instance.getNumNodes()];

            for (int i = 0; i < instance.getNumNodes(); i++) {
                satDegree[i] = new HashSet<Integer>();
            }

            for (int i = 0; i < instance.getNumNodes(); i++) {
                int minAllowed = Integer.MAX_VALUE;
                int currNode = -1;
                boolean[] usedColors = new boolean[k + 1];

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

                // fills up an array denoted which colors are used by the current nodes
                // neighbors
                for (int neighbor : instance.getAdjacent(currNode)) {
                    if (coloring[neighbor] > 0) {
                        usedColors[coloring[neighbor]] = true;
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
                coloring[currNode] = minColor;
                for (int neighbor : instance.getAdjacent(currNode)) {
                    satDegree[neighbor].add(minColor);
                }

            }

            // randomly colors any nodes that couldn't be colored without penalty
            // (aka where this.coloring[i] == -1)
            for (int i = 0; i < instance.getNumNodes(); i++) {
                if (coloring[i] == -1) {
                    coloring[i] = Heuristic.random(k) + 1;
                }
            }
            return coloring;
        }

        public class GarlinierTabuSearch extends TabuSearch {
            private int a;
            private int alpha;

            public GarlinierTabuSearch(int a, int alpha, SolutionConflictCounts solution) {
                tabuMap = new HashMap<>();
                this.a = a;
                this.alpha = alpha;
                this.solution = solution;
                this.A = new int[instance.getNumEdges() + 1];
                for (int i = 0; i < A.length; i++) {
                    this.A[i] = i - 1;
                }
            }

            public int getTenure() {
                return Heuristic.random(a) + alpha * nb_cfl;
            }
        }

        // this is different than the normal calc objective because it also updates
        // nb_cfl which will be used to
        // determine the length of the tabu tenure at each iteration
        public void calcObjective() {
            int obj = 0;
            conflictCount = new int[instance.getNumNodes() + 1];
            for (int i = 0; i < coloring.length; i++) {
                HashSet<Integer> adj = this.instance.getAdjacent(i);
                boolean conflictedNode = false;
                for (int adjv : adj) {
                    // If i < adjv, that edge hasn't been checked yet, this prevents from double
                    // counting
                    if (coloring[i] == coloring[adjv]) {
                        if (i < adjv) {
                            obj += 1;
                            conflictCount[i] += 1;
                            conflictCount[adjv] += 1;
                        }
                        if (!conflictedNode) {
                            nb_cfl++; // maybe consider making conflict count solution count the conflict vertices
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
            GarlinierTabuSearch ts = new GarlinierTabuSearch(A, alpha, this);
            ts.solution.heuristic = this.heuristic;
            int iteration = 0;

            while (objective > 0 && iteration < iterations && heuristic.report()) {
                Move neighbor = ts.generateBestRepNeighbor(rep, iteration);
                Move tabuMove = new Move(neighbor.node, this.coloring[neighbor.node], this);
                ts.tabuMap.put(tabuMove, iteration + ts.getTenure()); // a move is still tabu as long as the iteration is <=
                                                                 // curr iteration + tabuTenure
                iteration++;
            }
        }

        // maybe just have this return the cardinality class instead of class variable
        public int getMaxCardinalityClass() {
            int[] counts = new int[this.k + 1];
            int maxCardinality = -1;
            int maxCardinalityClass = 0;
            for (int i = 0; i < coloring.length; i++) {
                if (this.coloring[i] > 0) {
                    counts[this.coloring[i]]++;
                    if (counts[this.coloring[i]] > maxCardinality) {
                        maxCardinality = counts[this.coloring[i]];
                        maxCardinalityClass = this.coloring[i];
                    }
                }
            }
            return maxCardinalityClass;
        }

    }
}
