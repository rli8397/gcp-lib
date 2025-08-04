package papers;

import general.Heuristic;
import general.Instance;

//No new solution classes made, simple small changes, can keep it within solution 


public class Johnson1991Heuristic extends Heuristic {
        protected int k;

        public Johnson1991Heuristic(Instance i, double r) {
            super(i, r);
            
            this.k = i.getMaxChromatic();

            //according to paper, start k above max chromatic, which would be max degree + 1 + some arbitrary number
            Johnson1991Solution sol = new Johnson1991Solution(this, instance, k);

            do{
                sol.Johnson1991FixedK();
                
                if (sol.isValidSolution()){
                    this.k--;
                    sol.reduceK();
                }

            }while (report(sol));
            
        }

        public class Johnson1991Solution extends SolutionConflictCounts {

            public Johnson1991Solution(Heuristic h, Instance g, int k) {
                super(h, Solution.randomColoring(g.getNumNodes(), g.getMaxChromatic() ,h.getRandom()),k);
            }

            public void Johnson1991FixedK() {

                // Parameters
                double minpercent = .3;

                // Variable in paper, says it varies depending on the runtime limits
                double tempfactor = .95;
                double sizefactor = 0;
                // Described to be average size of neighborhood, which is confusing
                int ncap = 0;
                // Not specified
                int freezecount = 0;
                int freeze_lim = 10;

                int trials = 0;
                double t = 2.0;

                // Not specified
                int cutoff = 0;
                int change = 0;

                //report checker
                int iter  = 0;

                while (objective > 0 && freezecount < freeze_lim) {
                    change = 0;
                    trials = 0;
                    
                    //Checks every 1000 iterations if runtime has ended
                    if (iter % 1000 == 0 && !heuristic.report()){
                        break;
                    }

                    while (trials < sizefactor * ncap && change < cutoff * ncap) {

                        // Finding target node
                        Move target = randConflictedMove();

                        // Calculate Delta
                        double delta = target.getObjective() - objective;

                        trials += 1;

                        if (delta <= 0) {
                            makeMove(target);
                            change += 1;
                            freezecount = 0;
                            break;
                        } else {
                            double p = Math.exp(-delta / t);
                            double x = Math.random(); // One source of randomness

                            if (x < p) {
                                makeMove(target);
                                freezecount = 0;
                                change += 1;
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






    

    

