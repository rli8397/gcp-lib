package papers;

import general.Heuristic;
import general.Instance;

public class Chams1987Heuristic extends Heuristic {
    public Chams1987Heuristic(Instance instance, int runtime_limit) {
        super(instance, runtime_limit, instance.getMaxChromatic());
        Chams1987Solution solution = new Chams1987Solution(this, this.instance, this.k);
        while(this.report(solution)) {
            solution.reduceK();
            solution.Chams1987();
            solution.printStatus();
        }
        solution.printStatus();
    }

    public class Chams1987Solution extends SolutionConflictObjective {
        public Chams1987Solution(Heuristic heuristic, Instance instance, int k) {
            super(heuristic, Solution.randomColoring(heuristic, k), instance.getMaxChromatic());
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
                            coloring[move.node] = move.color;
                            this.objective += delta;
                            change = true;
                            break;
                            // Keep in mind k is updating and stuff
                        } else {
                            double p = Math.exp(-delta / t);
                            double x = Math.random();
                            if (x < p) {
                                coloring[move.node] = move.color;
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