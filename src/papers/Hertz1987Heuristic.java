package papers;

import general.*;
import general.HeuristicClasses.*;
import general.SolutionClasses.Solution;
import general.SolutionClasses.SolutionConflictCounts;

import java.util.*;
import general.Options;

public class Hertz1987Heuristic extends GCPWrapper {
    public Hertz1987Heuristic(Options options) {
        super(
                options,
                "random_restart");
    }

    public Hertz1987KCPHeuristic createKCPHeuristic(GCPHeuristic gcp, int k, int[] coloring) {
        return new Hertz1987KCPHeuristic(gcp, k, coloring);
    }

    public class Hertz1987KCPHeuristic extends KCPHeuristic {
        public Hertz1987KCPHeuristic(GCPHeuristic gcp, int k, int[] coloring) {
            super(gcp, k);
            try {
                int rep = Integer.parseInt(gcp.get_cmdline_arg("rep"));
                int nbmax = Integer.parseInt(gcp.get_cmdline_arg("nbmax"));
                int tenure = Integer.parseInt(gcp.get_cmdline_arg("tenure"));
                
                if (coloring == null) {
                    coloring = Solution.randomColoring(this.instance, this.instance.getMaxChromatic());
                }

                this.solution = new Hertz1987Solution(this, coloring, rep, nbmax, tenure);
            } catch (Exception e) {
                throw new IllegalArgumentException("Missing or invalid extended parameters for Hertz1987KCPHeuristic. Required parameters: rep, nbmax, tenure.");
            }

        }

        public Hertz1987KCPHeuristic(Options options, int k) {
            super(options, k);
            try {
                int rep = Integer.parseInt(options.extras.get("rep"));
                int nbmax = Integer.parseInt(options.extras.get("nbmax"));
                int tenure = Integer.parseInt(options.extras.get("tenure"));

                this.solution = new Hertz1987Solution(this, Solution.randomColoring(instance, k), rep, nbmax, tenure);
            } catch (Exception e) {
                throw new IllegalArgumentException("Missing or invalid extended parameters for Hertz1987KCPHeuristic. Required parameters: rep, nbmax, tenure.");
            }
        }

        public void run() {
            ((Hertz1987Solution) this.solution).tabuSearch();
            if (gcp != null) {
                gcp.report(this.solution);
            }
        }
    }

    public class Hertz1987Solution extends SolutionConflictCounts {
        // parameters
        // rep = 250;
        // tabuTenure = 7;
        // nbmax = 100000;
        private int rep;
        private int nbmax;
        private int tenure;

        public Hertz1987Solution(Hertz1987KCPHeuristic heuristic, int[] coloring, int rep, int nbmax, int tenure) {
            super(heuristic.getInstance(), coloring, heuristic.getK());
            this.rep = rep;
            this.nbmax = nbmax;
            this.tenure = tenure;
        }

        public class HertzTabuSearch extends TabuSearch<Move> {
            public HertzTabuSearch(SolutionConflictCounts solution) {
                super(solution);
            }

            public Move generateBestNeighbor(int iteration) {
                if (threeMoveSearch()) {
                    return null;
                }

                return generateBestRepNeighbor(iteration);
            }

            public Move generateBestRepNeighbor(int iteration) {
                // in this paper, only a "rep" number of neighbors are generated
                // out of those rep neighbors, the best is then returned
                int bestObj = Integer.MAX_VALUE;
                Move bestMove = null;
                int i = 0;
                int loopCount = 1;

                while (i < rep) {
                    // if 1000 iterations have passed without finding a single possible neighbor
                    // we are assuming that a neighbor can't be found with
                    // the given criteria and are going to return any random move
                    // this is a judgement call and is not stated in the paper
                    if (loopCount % 1000 == 0 && i == 0) {
                        // if no move has been made after 1000 iterations, return any random move
                        return solution.randMove();
                    }

                    Move currMove = randConflictedMove();
                    int currObj = currMove.getObjective();
                    if (!isTabu(currMove, iteration) || currObj <= A[objective]) {
                        if (currObj < bestObj) {
                            bestObj = currObj;
                            bestMove = currMove;
                            if (bestObj <= A[objective]) {
                                A[objective] = bestObj - 1;
                            }

                            if (bestObj < objective) {
                                break;
                            }
                        }
                        i += 1;
                    }
                    loopCount++;

                }
                return bestMove;
            }

            public boolean stopCondition(int iteration) {
                return solution.isValidSolution() || !report() || iteration >= nbmax;
            }

            public void tabuAppend(Move move, int iteration) {
                Move tabuMove = new Move(move.getNode(), solution.getColoring()[move.getNode()], solution);
                // a move is still tabu as long as the iteration is
                // <= curr iteration + tabuTenure
                tabuMap.put(tabuMove, iteration + tenure);
            }

            public void makeMove(Move move) {
                solution.makeMove(move);
            }
        }

        public int centralConflictNode() {
            for (int node : conflictCount.keySet()) {
                if (conflictCount.get(node) == objective) {
                    return node;
                }
            }
            return -1;
        }

        public HashSet<Integer> avaliableColors(int node, int centralNode, int centralNodeColor) {
            HashSet<Integer> avaliableColors = new HashSet<>();

            for (int c = 1; c <= k; c++) {
                avaliableColors.add(c);
            }

            avaliableColors.remove(centralNodeColor);
            for (int neighbor : instance.getAdjacent(node)) {
                if (neighbor != centralNode) {
                    avaliableColors.remove(coloring[neighbor]);
                }
            }

            return avaliableColors;
        }

        public boolean threeMoveSearch() {
            int centralNode = centralConflictNode();

            if (centralNode == -1) {
                return false;
            }

            for (int c = 1; c <= k; c++) {
                ArrayList<Integer> conflicts = new ArrayList<>();
                for (int neighbor : instance.getAdjacent(centralNode)) {
                    if (coloring[neighbor] == c) {
                        conflicts.add(neighbor);
                    }
                }

                if (conflicts.size() == 0) {

                    this.coloring[centralNode] = c;
                    return true;

                } else if (conflicts.size() == 1) {

                    int n = conflicts.get(0);

                    HashSet<Integer> colors = avaliableColors(n, centralNode, c);

                    if (colors.size() != 0) {
                        this.coloring[centralNode] = c;
                        this.coloring[n] = colors.iterator().next();
                        return true;
                    }

                } else if (conflicts.size() == 2) {

                    int n1 = conflicts.get(0);
                    int n2 = conflicts.get(1);

                    HashSet<Integer> colors1 = avaliableColors(n1, centralNode, c);
                    HashSet<Integer> colors2 = avaliableColors(n2, centralNode, c);

                    if (instance.getAdjacent(n1).contains(n2)) {
                        for (int i : colors1) {
                            if (colors2.contains(i)) {
                                colors1.remove(i);
                                colors2.remove(i);
                                break;
                            }
                        }
                    }

                    if (colors1.size() != 0 && colors2.size() != 0) {
                        this.coloring[centralNode] = c;
                        this.coloring[n1] = colors1.iterator().next();
                        this.coloring[n2] = colors2.iterator().next();
                        return true;
                    }
                }

            }

            return false;
        }

        // this is the tabucol local search that will run
        public void tabuSearch() {

            HertzTabuSearch ts = new HertzTabuSearch(this);
            ts.tenure = this.tenure;
            ts.hertzTabuSearch();
        }
    }

}
