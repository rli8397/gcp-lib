
package papers;
import java.util.*;

import general.Heuristic;
import general.Instance;
import general.Options;

public class Han2010Heuristic extends Heuristic{
    private Han2010Solution[] population;
    private int k; // current color limit
    private double pc = 0.8; // crossover probability
    private double pm = 0.2; // mutation probability
    private Random rand = new Random();

    public Han2010Heuristic(Options options) {
        super(options);
        k = options.instance.getMaxChromatic();

        InitPopulation(Integer.parseInt(options.extras.get(0).trim()));

        Han2010Solution best = population[0];
        do {
            // create offspring via crossover and mutation
            List<Han2010Solution> offspring = new ArrayList<>();

            // Pairwise crossover
            for (int i = 0; i < population.length / 2; i++) {
                int p1 = random(population.length);
                int p2 = random(population.length);
                while (p2 == p1) p2 = random(population.length);

                Han2010Solution parent1 = population[p1];
                Han2010Solution parent2 = population[p2];

                if (rand.nextDouble() < pc) {
                    int[][] kids = crossOver(parent1, parent2);
                    offspring.add(new Han2010Solution(this, kids[0], k));
                    offspring.add(new Han2010Solution(this, kids[1], k));
                } else {
                    offspring.add(new Han2010Solution(this, parent1.coloring.clone(), k));
                    offspring.add(new Han2010Solution(this, parent2.coloring.clone(), k));
                }
            }

            // Mutation
            List<Han2010Solution> mutated = new ArrayList<>();
            for (Han2010Solution sol : offspring) {
                if (rand.nextDouble() < pm) {
                    mutate(sol);
                }
                mutated.add(sol);
            }

            // Selection
            population = paretoSelection(population, offspring.toArray(new Han2010Solution[0]),
                                         mutated.toArray(new Han2010Solution[0]), popSize);

            // track best
            for (Han2010Solution sol : population) {
                if (sol.getObjective() < best.getObjective() ||
                    (sol.getObjective() == 0 && sol.calcK() < best.calcK())) {
                    best = sol;
                }
            }

        } while (report(best));
    }

    private void InitPopulation(int popSize) {
        population = new Han2010Solution[popSize];
        for (int i = 0; i < popSize; i++) {
            if (report()) {
                int[] coloring = Solution.greedyConstruction(getInstance(), k, this);
                population[i] = new Han2010Solution(this, coloring, k);
            }
        }
    }

    
     //Largest-set-first relabeling heuristic
    
    private void relabelByLargestSet(Han2010Solution sol) {
        // count sizes of color classes
        Map<Integer, Integer> counts = new HashMap<>();
        for (int c : sol.coloring) {
            counts.put(c, counts.getOrDefault(c, 0) + 1);
        }
        // sort colors by decreasing size
        List<Integer> colors = new ArrayList<>(counts.keySet());
        colors.sort((a, b) -> Integer.compare(counts.get(b), counts.get(a)));
        // assign new labels 1..m
        Map<Integer, Integer> relabelMap = new HashMap<>();
        int newLabel = 1;
        for (int oldColor : colors) {
            relabelMap.put(oldColor, newLabel++);
        }
        for (int i = 0; i < sol.coloring.length; i++) {
            sol.coloring[i] = relabelMap.get(sol.coloring[i]);
        }
    }

   
    private int[][] crossOver(Han2010Solution s1, Han2010Solution s2) {
        Han2010Solution p1 = s1.copy(this);
        Han2010Solution p2 = s2.copy(this);

        relabelByLargestSet(p1);
        relabelByLargestSet(p2);

        int n = instance.getNumNodes();
        int[] child1 = new int[n];
        int[] child2 = new int[n];

        // child1 from p1's perspective
        for (int i = 0; i < n; i++) {
            if (p1.hasConflict(i)) {
                child1[i] = p2.coloring[i];
            } else {
                child1[i] = Math.min(p1.coloring[i], p2.coloring[i]);
            }
        }
        // child2 from p2's perspective
        for (int i = 0; i < n; i++) {
            if (p2.hasConflict(i)) {
                child2[i] = p1.coloring[i];
            } else {
                child2[i] = Math.min(p1.coloring[i], p2.coloring[i]);
            }
        }

        return new int[][]{child1, child2};
    }

    
     //Mutation: random reassignment of colors
     
    private void mutate(Han2010Solution sol) {
        for (int i = 0; i < sol.coloring.length; i++) {
            sol.coloring[i] = random(k) + 1;
        }
    }

    
      //Pareto selection
     
    private Han2010Solution[] paretoSelection(Han2010Solution[] pop, Han2010Solution[] off1,
                                          Han2010Solution[] off2, int popSize) {
        List<Han2010Solution> combined = new ArrayList<>();
        combined.addAll(Arrays.asList(pop));
        combined.addAll(Arrays.asList(off1));
        combined.addAll(Arrays.asList(off2));

        // rank by Pareto dominance
        combined.sort((a, b) -> {
            int rA = dominanceRank(a, combined);
            int rB = dominanceRank(b, combined);
            if (rA != rB) return Integer.compare(rA, rB);
            // tie-break by p(x) = conflicts
            return Integer.compare(a.getObjective(), b.getObjective());
        });

        return combined.subList(0, popSize).toArray(new Han2010Solution[0]);
    }

    private int dominanceRank(Han2010Solution sol, List<Han2010Solution> all) {
        int rank = 0;
        for (Han2010Solution other : all) {
            if (dominates(other, sol)) rank++;
        }
        return rank;
    }

    private boolean dominates(Han2010Solution a, Han2010Solution b) {
        int kA = a.calcK(), kB = b.calcK();
        int pA = a.getObjective(), pB = b.getObjective();
        return (kA <= kB && pA <= pB) && (kA < kB || pA < pB);
    }

    public class Han2010Solution extends SolutionConflictCounts {

        public Han2010Solution(Heuristic heuristic, int[] coloring, int colors) {
            super(heuristic, coloring, colors);
            calcObjective();
        }

        public Han2010Solution copy(Han2010Heuristic heuristic) {
            return new Han2010Solution(heuristic, coloring.clone(), k);
        }

        public boolean hasConflict(int vertex) {
            for (int neighbor : instance.getAdjacent(vertex)) {
                if (coloring[neighbor] == coloring[vertex]) return true;
            }
            return false;
        }
        
    }
}
 