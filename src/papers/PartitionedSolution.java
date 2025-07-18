package papers;

import java.util.*;

import general.Heuristic;
import general.Instance;

public class PartitionedSolution extends Solution {
    // cardinality refers to the size of a color class
    private int maxCardinalityClass; 
    // index i represents color class i + 1; coloring[i] represents the set of
    // vertices belonging to color class i
    private HashSet<Integer>[] partitionedColoring;

    public PartitionedSolution(Heuristic heuristic, Instance instance, int colors) {
        super(heuristic, colors, instance, false, false);
        this.partitionedColoring = new HashSet[colors]; 
        this.maxCardinalityClass = 0;
        for (int i = 0; i < k; i++) {
            partitionedColoring[i] = new HashSet<>();
        }
        randomColoring();
    }

    // produces a random partitioned coloring of the graph
    public void randomColoring() {
        for (int i = 0; i < this.instance.getNumNodes(); i++) {
            int color = rand.nextInt(k);
            this.partitionedColoring[color].add(i);
        }
        calcObjective();
        calcMaxCardinality();
    }

    public void calcMaxCardinality() {
        this.maxCardinalityClass = 0;
        for (int i = 0; i < k; i++) {
            int cardinality = partitionedColoring[i].size();
            if (cardinality > partitionedColoring[maxCardinalityClass].size()) {
                maxCardinalityClass = k;
            }
        }
    }
    
    public void reduceK() {

    }

    public int getMaxCardinalityClass() {
        return maxCardinalityClass;
    }
}
