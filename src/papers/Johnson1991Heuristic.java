package papers;

import general.Instance;
import general.Move;
import general.HeuristicClasses.*;
import general.SolutionClasses.Solution;
import general.SolutionClasses.SolutionConflictCounts;

//No new solution classes made, simple small changes, can keep it within solution 

public class Johnson1991Heuristic extends GCPWrapper<Johnson1991Heuristic.Johnson1991KCPHeuristic> {

    public Johnson1991Heuristic(Instance instance, double runtime_limit) {
        super(
            instance, 
            runtime_limit, 
            Johnson1991KCPHeuristic.class, 
            Solution.randomColoring(instance, instance.getMaxChromatic())
        );
    }

    
    public class Johnson1991KCPHeuristic extends KCPHeuristic<Johnson1991Solution> {
        public Johnson1991KCPHeuristic (Johnson1991Heuristic wrapper, int k) {
            super(wrapper, k, Johnson1991Solution.class);
            Johnson1991Solution solution = new Johnson1991Solution(wrapper, wrapper.getColoring(), k);
            solution.Johnson1991FixedK();
            wrapper.report(solution, k);
        }
    }
    
    public class Johnson1991Solution extends SolutionConflictCounts {
        public Johnson1991Solution(GCPHeuristic heuristic, int[] coloring, int k) {
            super(heuristic, coloring, k);
        }

        public void Johnson1991FixedK() {

            // Parameters
            double minpercent = .3;

            // Variable in paper, says it varies depending on the runtime limits
            double tempfactor = .95;
            double sizefactor = 0;
            // Described to be average size of neighborhood, which is confusing
            int ncap = 0;

            int freezecount = 0;
            int freeze_lim = 10;

            int trials = 0;
            double t = 2.0;

            // Not specified
            int cutoff = 0; // cutoff in paper says 0.10
            int change = 0;

            // report checker
            int iter = 0;

            while (objective > 0 && freezecount < freeze_lim) {
                change = 0;
                trials = 0;

                // Checks every 1000 iterations if runtime has ended
                if (iter % 1000 == 0 && !heuristic.report()) {
                    break;
                }

                while (trials < sizefactor * ncap && change < cutoff * ncap) {
                    trials += 1;

                    // Finding target node
                    Move target = randConflictedMove();

                    // Calculate Delta
                    double delta = target.getObjective() - objective;

                    if (delta <= 0) {
                        change += 1;
                        makeMove(target);
                        freezecount = 0; // read over freezecount here, it should only be reset if a best objective is found

                        break;
                    } else {

                        double r = Math.random(); // the paper says that 1 should be inclusive, does this matter?
                        double p = Math.exp(-delta / t);

                        if (r <= p) {
                            change += 1;
                            makeMove(target);
                            break;
                        }
                    }
                }
                t *= tempfactor; // Decreases temperature

                // If enough changes per trial is too low, it's freezing. Changes have to be
                // happening
                if (change / trials < minpercent) {
                    freezecount += 1;
                }

                iter++;
            }

        }

    }

}
