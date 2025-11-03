package papers;

import java.util.ArrayList;

import general.Instance;
import general.HeuristicClasses.*;
import general.SolutionClasses.PartialColoring;
import general.*;


public class Morgenstern1996Heuristic extends Heuristic {
    public Morgenstern1996Heuristic(Instance instance, double runtime, int popSize) {
        super(instance, runtime, instance.getMaxChromatic());
    }


    public class Morgenstern1996Solution extends PartialColoring {
        public Morgenstern1996Solution(Heuristic heuristic, int[] coloring, int k) {
            super(heuristic, coloring, k);
            calcObjective();

            //objective is the sum of the degrees of uncolored nodes
        }

        public void calcObjective() {
            int obj = 0; 

            for (int node : uncolored) {
                obj += instance.getDegree(node);
            }
            
            this.objective = obj;
        }
    }
    
}
