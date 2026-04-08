package papers;

import general.*;
import general.HeuristicClasses.*;
import general.SolutionClasses.Solution;
import general.SolutionClasses.SolutionConflictCounts;
import general.SolutionClasses.SolutionConflictObjective;

import java.util.*;

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

        public class Hertz1987TabuSearch extends HertzTabuSearch {
            public Hertz1987TabuSearch(SolutionConflictObjective solution, int tenure) {
                super(solution, Hertz1987Heuristic.this);
                this.tenure = tenure;
            }

            public Move generateBestNeighbor(int iteration) {
                // breaks the loop if this method returns null
                // if threeMoveSearch is found, means a valid solution is found
                if (threeMoveSearch()) {
                    return null;
                }

                return generateBestRepNeighbor(iteration, rep);
            }

            public boolean stopCondition(int iteration) {
                return solution.isValidSolution() || !report() || iteration >= nbmax;
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


        // A 3 move is a move that changes up to 3 colors at once
        // This will be triggered when there is a central node that contains all the conflicted edges
        // This method will edit the coloring directly if a 3-move is found and return true
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
            Hertz1987TabuSearch ts = new Hertz1987TabuSearch(this, this.tenure);
            ts.tabuSearch();
        }
    }

}
