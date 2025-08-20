package papers;

import general.Heuristic;
import general.Instance;
import papers.Garlinier1999Heuristic.Garlinier1999Solution;
import papers.Glass2003Heuristic.Glass2003Solution;

public class GASkeleton extends Heuristic {
    private GASkeletonSolution[] population;
    private int k;

    public GASkeleton(Instance instance, double runtime, int popSize) {
        super(instance, runtime);
        System.out.println("Running heuristic");
        GASkeletonSolution solution;
        k = instance.getMaxChromatic();
        InitPopulation(popSize);
        do {
            // chooses 2 random parents
            int s1 = random(population.length);
            int s2 = random(population.length);
            while (s2 == s1) {
                s2 = random(population.length);
            }

            solution = new GASkeletonSolution(this, crossOver(population[s1], population[s2]), this.k);

            // if a valid solution is found, we will restart the algorithm looking for k - 1
            // colors
            if (solution.objective == 0) {
                int newK = calcK(solution);
                this.k = newK - 1;
                solution.k = newK;
                InitPopulation(popSize);
            } else {
                // updatePopulation
                int toReplace = s1;
                if (population[s1].getObjective() < population[s2].getObjective()) {
                    toReplace = s2;
                }
                population[toReplace] = solution;
            }
        } while (report(solution));
    }

    public void InitPopulation(int popSize) {
        this.population = new GASkeletonSolution[popSize];
        for (int i = 0; i < popSize; i++) {
            population[i] = new GASkeletonSolution(this, Solution.greedyConstruction(this, this.k), this.k);
        }
    }

    public int[] crossOver(GASkeletonSolution s1, GASkeletonSolution s2) {
        int[] coloring = new int[instance.getNumNodes()];
        for (int l = 1; l <= k; l++) {
            if (l % 2 == 1) {
                int color = s1.getMaxCardinalityClass();
                for (int i = 0; i < coloring.length; i++) {
                    if (s1.coloring[i] == color) {
                        s1.coloring[i] = -1;
                        s2.coloring[i] = -1;
                        coloring[i] = l;
                    }
                }
            } else {
                int color = s2.getMaxCardinalityClass();
                for (int i = 0; i < coloring.length; i++) {
                    if (s2.coloring[i] == color) {
                        s1.coloring[i] = -1;
                        s2.coloring[i] = -1;
                        coloring[i] = l;
                    }
                }
            }
        }
        
        // if there are leftover nodes, just randomly assign them
        for (int i = 0; i < coloring.length; i++) {
            if (coloring[i] <= 0) {
                coloring[i] = random(this.k) + 1;
            }
        }

        return coloring;
    }

    // calculates how many colors are used ina  solution, this is used if a solution is found with
    // a lower k than the target k
    public int calcK(GASkeletonSolution sol){
        boolean[] visited = new boolean [this.k+1];

        int count = 0;
        for (int c : sol.coloring){
            //Unique Color
            if (!visited[c]){
                visited[c] = true;
                count++;
            }
        }

        return count;
    }

    public class GASkeletonSolution extends SolutionConflictObjective {
        public GASkeletonSolution(Heuristic heuristic, int[] coloring, int colors) {
            super(heuristic, coloring, colors);
        }

        // returns the color partition with the most nodes 
        public int getMaxCardinalityClass() {
            int[] counts = new int[this.k + 1];
            int maxCardinality = -1;
            int maxCardinalityClass = 0;
            for (int i = 0; i < coloring.length; i++) {
                if (this.coloring[i] > 0) {
                    counts[this.coloring[i]]++;
                    if (counts[this.coloring[i]] > maxCardinality) {
                        maxCardinality = counts[this.coloring[i]];
                        maxCardinalityClass = this.coloring[i];
                    }
                }
            }
            return maxCardinalityClass;
        }
    }
}
