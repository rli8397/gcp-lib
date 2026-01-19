package papers;
import general.*;
import general.SolutionClasses.*;
import general.HeuristicClasses.*;

public class Chams1987Heuristic extends GCPWrapper {
    public Chams1987Heuristic(Options options) {
        super (
            options,
            "random_restart"
        );
    }

    public Chams1987KCPHeuristic createKCPHeuristic(GCPHeuristic gcp, int k, int[] coloring) {
        return new Chams1987KCPHeuristic(gcp, k, coloring);
    }

    public class Chams1987KCPHeuristic extends KCPHeuristic {
        public Chams1987KCPHeuristic(GCPHeuristic gcp, int k, int[] coloring) {
            super(gcp, k);
            if (coloring == null) {
                coloring = Solution.randomColoring(this.instance, this.instance.getMaxChromatic());
            }
            this.solution = new Chams1987Solution(this, coloring);

            if (verbosity == 3) {
                System.out.println("[DEBUG] Initial solution created with objective="
                        + ((Chams1987Solution)this.solution).getObjective());
            }

        }

        public void run() {
            ((Chams1987Solution) solution).Chams1987();
        }

    }

    public class Chams1987Solution extends SolutionConflictObjective {
        public Chams1987Solution(Chams1987KCPHeuristic heuristic, int[] coloring) {
            super(heuristic.getInstance(), coloring, heuristic.getK());
        }

        public void Chams1987() {

             if (verbosity == 3) {
                    System.out.println("[DEBUG] Entering Chams1987 simulated annealing loop");
                    //Inital Status 
                    printStatus();
             }

            while (objective > 0 && report(this)) {
                double t = Math.sqrt(this.instance.getNumNodes());
                double a = 0.93;
                boolean change = true;

                if (verbosity == 3) {
                    System.out.println("[DEBUG] New outer iteration: objective="
                            + objective + ", temperature=" + t);
                }

                while (change) {
                    change = false;
                    int rep = (int) Math.exp(2 / t);

                    if (verbosity == 3) {
                        System.out.println("[DEBUG] Inner loop started: rep=" + rep
                                + ", temperature=" + t);
                    }

                    for (int i = 0; i < rep; i++) {
                        Move move = randMove();
                        double delta = move.getObjective() - this.getObjective();

                        if (verbosity == 3) {
                            System.out.println("[DEBUG] Inner loop started: rep=" + rep
                                + ", temperature=" + t);
                        }

                        if (delta < 0) {
                            coloring[move.getNode()] = move.getColor();
                            this.objective += delta;
                            change = true;

                            if (verbosity == 3) {
                                System.out.println("[DEBUG] Accepted improving move, new objective="
                                        + this.objective);
                                //Print Solution
                                printStatus();
                            }

                            
                            break;
                            // Keep in mind k is updating and stuff
                        } else {
                            double p = Math.exp(-delta / t);
                            double x = Math.random();

                            if (verbosity == 3) {
                                System.out.println("[DEBUG] Non-improving move: p=" + p
                                        + ", rand=" + x);
                            }

                            if (x < p) {
                                coloring[move.getNode()] = move.getColor();
                                this.objective += delta;
                                if (delta != 0) {
                                    change = true;
                                }

                                if (verbosity == 3) {
                                    System.out.println("[DEBUG] Accepted probabilistic move, new objective="
                                            + this.objective);
                                    //Print Solution
                                    printStatus();
                                }

                                break;
                            }
                        }
                    }
                    t *= a; // Decreases temperature
                    if (verbosity == 3) {
                        System.out.println("[DEBUG] Temperature decreased to " + t);                   
                    }
                }
            }

        }
    }
}