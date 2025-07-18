package papers;
import general.Heuristic;
import general.Instance;

public class PairedSolution extends Solution2<Integer> {
    protected int[] coloring; // colors start from 1 to k, 0 indicates uncolored node
    public PairedSolution(Heuristic heuristic, int colors, Instance instance, boolean random, boolean stable) {
        this.heuristic = heuristic;
        this.k = colors; 
        this.instance = instance;
        this.coloring = new int[instance.getNumNodes()];
        randomColoring();
        calcObjective();
    }

 
}
