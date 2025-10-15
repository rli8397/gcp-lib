package papers;

import general.Heuristic;
import general.Instance;
import general.Options;


public class Chams1987Heuristic extends Heuristic {
    public Chams1987Heuristic(Options params) {
        super(params);
        Chams1987Solution solution = new Chams1987Solution(this, Solution.randomColoring(this, Heuristic.getRandom().nextInt()), instance.getMaxChromatic());
        while(this.report(solution)) {
            solution.reduceK();
            solution.Chams1987();
            solution.printStatus();
        }
        solution.printStatus();
    }

    public class Chams1987Solution extends SolutionConflictObjective {
        public Chams1987Solution(Heuristic heuristic, int[] coloring,int colors) {
            super(heuristic, coloring, colors);
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
                        Move move = generateRandomMove();
                        double delta = calcNeighborObjective(move) - this.getObjective();
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