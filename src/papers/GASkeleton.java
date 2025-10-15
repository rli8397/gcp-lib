package papers;

import java.util.HashSet;

import general.Heuristic;
import general.Instance;
import papers.Garlinier1999Heuristic.Garlinier1999Solution;
import papers.Glass2003Heuristic.Glass2003Solution;
import general.Options;


public class GASkeleton extends Heuristic {
    private GASkeletonSolution[] population;
    private int k;

    public GASkeleton(Options params, int popSize) {
        super(params);
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

            //Instantiates the appropriate Solution type
            solution  = createSolution(this, crossOver(population[s1], population[s2]), this.k);

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
        GAGreedyConstructor dsatur = new GAGreedyConstructor(this, this.k);

        this.population = new GASkeletonSolution[popSize];
        for (int i = 0; i < popSize; i++) {
            population[i] = new GASkeletonSolution(this, dsatur.newGreedy(), this.k);
            population[i] = createSolution (this, dsatur.newGreedy(), this.k);
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

    //Placeholder
    public GASkeletonSolution createSolution(Heuristic h, int[] coloring, int colors){
        return null;
    }

    public class GAGreedyConstructor{
        int[] template;
        int k;
        Heuristic heuristic;
        
        //Constructs the deterministic fragment of DSATUR once for a given K
        public GAGreedyConstructor(Heuristic heuristic, int k){
            this.k = k;
            this.heuristic = heuristic;

            //Initialization
            Instance instance = heuristic.getInstance();
            HashSet<Integer>[] satDegree = new HashSet[instance.getNumNodes()];
            int[] coloring  = new int[instance.getNumNodes()];

            for (int i = 0; i < instance.getNumNodes(); i++) {
                satDegree[i] = new HashSet<Integer>();
            }

            for (int i = 0; i < instance.getNumNodes(); i++) {
                int minAllowed = Integer.MAX_VALUE;
                int currNode = -1;
                //boolean[] usedColors = new boolean[k + 1];

                // loops through all nodes and searchs for the node with the minimum colors
                // allowed without giving penalty
                for (int node = 0; node < instance.getNumNodes(); node++) {
                    if (coloring[node] == 0) { // Note: 0 means unvisited
                        int allowed = k - satDegree[node].size();
                        // -1 will notate that there is no allowed color
                        // these nodes will be randomly colored later
                        if (allowed == 0) {
                            coloring[node] = -1;
                        } else if (allowed < minAllowed) {
                            minAllowed = allowed;
                            currNode = node;
                        }
                    }
                }

                // if no new node is able to be colored without penalty, break
                if (currNode == -1) {
                    break;
                }
                
                /* 
                // fills up an array denoted which colors are used by the current nodes
                // neighbors
                for (int neighbor : instance.getAdjacent(currNode)) {
                    if (coloring[neighbor] > 0) {
                        usedColors[coloring[neighbor]] = true;
                    }
                }
                */

                int minColor = -1;
                for (int j = 1; j <= k; j++){
                    if (!satDegree[i].contains(j)){
                        minColor = j;
                        break;
                    }
                }

                /* 
                // finds the smallest color class that is not used by the current nodes
                // neighbors
                int minColor = -1;
                for (int j = 1; j < usedColors.length; j++) {
                    if (!usedColors[j]) {
                        minColor = j;
                        break;
                    }
                }
                */

                // currNode is assigned minColor, then updates the sat degree of all its
                // neighbors by adding the minColor to the set of colors neighboring the
                // neighbor
                coloring[currNode] = minColor;
                for (int neighbor : instance.getAdjacent(currNode)) {
                    satDegree[neighbor].add(minColor);
                }


            }
        }

        public int[] newGreedy(){
            int[] coloring  = new int[template.length];
            

            //Completes the stochastic process
            for (int i = 0; i < template.length; i++){
                if (template[i] == -1){
                    coloring[i] = heuristic.random(k) + 1;
                }
                else{
                    coloring[i] = template[i];
                }
            }

            return coloring;
        }
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
