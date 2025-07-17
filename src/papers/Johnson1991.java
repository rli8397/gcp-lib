package papers;

import java.util.*;

import general.Heuristic;
import general.Instance;

//No new solution classes made, simple small changes, can keep it within solution 
public class Johnson1991 {

    public class Johnson1991Solution extends SolutionConflict {

        public Johnson1991Solution(int colors, Instance g) {
            super(colors, g, true, false);
        }

        public Johnson1991Solution(Johnson1991Solution other) {
            super(other);
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

            while (objective > 0 && freezecount < freeze_lim) {
                change = 0;
                trials = 0;

                while (trials < sizefactor * ncap && change < cutoff * ncap) {

                    // Finding target node
                    int target = generateRandConflictedNode();

                    int newColor = 0;

                    // Switching color randomly 1 to k
                    do {
                        newColor = (int) (Math.random() * k) + 1;
                    } while (newColor != coloring[target]);

                    // Calculate Delta
                    double delta = calcNeighborObjective(new Move(target, newColor)) - objective;

                    trials += 1;

                    if (delta < 0) {
                        coloring[target] = newColor;
                        objective += delta;
                        change += 1;
                        freezecount = 0;
                        break;
                    } else {
                        double p = Math.exp(-delta / t);
                        double x = Math.random(); // One source of randomness

                        if (x < p) {
                            coloring[target] = newColor;
                            objective += delta;
                            freezecount = 0;
                            
                            //Check this
                            if (delta != 0) {
                                change += 1;
                            }
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
            }

        }

        /*
         * //FixedK method calls for switching a random node with conflicting edges to a
         * new color
         * public Johnson1991Solution generateNewNeighbor(){
         * 
         * //This bad -- don't need to make an entire new solution for one change
         * Johnson1991Solution neighbor = new Johnson1991Solution(this);
         * 
         * //Selecting Random node -- O(n) //promote this procedure
         * //Maybe replace this with a HASHMAP
         * ArrayList<Integer> indicies = new ArrayList<Integer>();
         * for (int i = 0; i < concount.length; i ++){
         * if (concount[i] > 0){
         * indicies.add(i);
         * }
         * }
         * int random_node = (int)(Math.random()*indicies.size());
         * 
         * int newColor = 0;
         * 
         * //Switching color randomly 1 to k
         * do{
         * newColor = (int) (Math.random()*k) + 1;
         * }while (newColor != neighbor.coloring[random_node]);
         * 
         * neighbor.coloring[random_node] = newColor;
         * 
         * //Recalculating Conflict promotote this procedure
         * int originalConflict = neighbor.concount[random_node];
         * int newConflict = 0;
         * 
         * //O(1)
         * HashSet<Integer> adj = graph.getAdjacent(random_node);
         * 
         * //O(n)
         * for (int adjv : adj){
         * if (neighbor.coloring[random_node] == coloring[adjv]){
         * newConflict +=1;
         * }
         * }
         * 
         * //Update Objective -- O(1)
         * neighbor.objective += newConflict - originalConflict;
         * 
         * return neighbor;
         * }
         * 
         */
    }

    public class Johnson1991Heuristic extends Heuristic {
        
        public Johnson1991Heuristic(Instance i, double r) {
            super(i, r);
            
            heuristic();
        }

        public Johnson1991Solution heuristic (){            
            //according to paper, start k above max chromatic, which would be max degree + 1 + some arbitrary number
            int k = instance.getMaxChromatic();

            Johnson1991Solution bestSolution  = null;

            while (report()){
                Johnson1991Solution sol = new Johnson1991Solution(k, instance);

                sol.Johnson1991FixedK();

                //Successful k-coloring
                
                if (sol.objective == 0 && report()){
                    bestSolution = new Johnson1991Solution(sol); // deep copy
                    System.out.println("k: " + k);
                    System.out.println(sol);
                    k--;
                }
                
            }

            if (bestSolution != null){
                System.out.println("Best Coloring: " + bestSolution.k);
            }

            return bestSolution;
        }

        
    }
}
