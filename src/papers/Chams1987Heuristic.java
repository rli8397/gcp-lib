package papers;
import general.*;
import general.SolutionClasses.*;
import general.HeuristicClasses.*;

public class Chams1987Heuristic extends GCPWrapper<Chams1987Heuristic.Chams1987KCPHeuristic> {
    public Chams1987Heuristic(Options options) {
        super (
            options,
            Solution.randomColoring(options.instance, options.instance.getMaxChromatic()),
            "random_restart"
        );
    }

    public Chams1987KCPHeuristic createKCPHeuristic(GCPHeuristic gcp, int k) {
        return new Chams1987KCPHeuristic(gcp, k);
    }

    public class Chams1987KCPHeuristic extends KCPHeuristic<Chams1987Solution> {
        public Chams1987KCPHeuristic(GCPHeuristic gcp, int k) {
            super(gcp, k);
            this.solution = new Chams1987Solution(this, gcp.getColoring());
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
            while (objective > 0 && report()) {
                double t = Math.sqrt(this.instance.getNumNodes());
                double a = 0.93;
                boolean change = true;
                while (change) {
                    change = false;
                    int rep = (int) Math.exp(2 / t);
                    for (int i = 0; i < rep; i++) {
                        Move move = randMove();
                        double delta = move.getObjective() - this.getObjective();
                        if (delta < 0) {
                            coloring[move.getNode()] = move.getColor();
                            this.objective += delta;
                            change = true;
                            break;
                            // Keep in mind k is updating and stuff
                        } else {
                            double p = Math.exp(-delta / t);
                            double x = Math.random();
                            if (x < p) {
                                coloring[move.getNode()] = move.getColor();
                                this.objective += delta;
                                if (delta != 0) {
                                    change = true;
                                }
                                break;
                            }
                        }
                    }
                    t *= a; // Decreases temperature
                }
            }

        }
    }
}