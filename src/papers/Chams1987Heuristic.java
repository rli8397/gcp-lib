package papers;

import general.Instance;
import general.Move;
import general.HeuristicClasses.*;
import general.SolutionClasses.Solution;
import general.SolutionClasses.SolutionConflictObjective;

public class Chams1987Heuristic extends GCPWrapper<Chams1987Heuristic.Chams1987KCPHeuristic> {
    public Chams1987Heuristic(Instance instance, double runtime_limit) {
        super (
            instance, 
            runtime_limit,
            Chams1987KCPHeuristic.class,
            Solution.randomColoring(instance, instance.getMaxChromatic())
        );
    }

    public class Chams1987KCPHeuristic extends KCPHeuristic<Chams1987Solution> {
        public Chams1987KCPHeuristic(Chams1987Heuristic wrapper, int k) {
            super(wrapper, k, Chams1987Solution.class);
            Chams1987Solution solution = new Chams1987Solution(wrapper, wrapper.getColoring(), k);
            wrapper.report(solution, k);
        }
    }

    public class Chams1987Solution extends SolutionConflictObjective {
        public Chams1987Solution(GCPHeuristic heuristic, int[] coloring, int k) {
            super(heuristic, coloring , k);
        }

        public void Chams1987() {
            while (objective > 0 && heuristic.report()) {
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