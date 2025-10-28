package general.SolutionClasses;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;

import general.Instance;
import general.Move;
import general.HeuristicClasses.*;

public class PartialColoring extends Solution {
    protected int objective; // objective is the number of uncolored nodes
    protected HashSet<Integer> uncolored;
    
    public PartialColoring(Instance instance, int[] coloring, int k) {
        super(instance, coloring, k);
    }

    public static int[] partialColoring(GCPHeuristic heuristic, int k) {
        Instance instance = heuristic.getInstance();
        int n = instance.getNumNodes();
        int[] coloring = new int[n];

        // convert adjancey set to bitset form
        List<BitSet> adjacencyList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            BitSet bs = new BitSet(n);
            for (int neighbor : instance.getAdjacent(i)) {
                bs.set(neighbor);
            }
            adjacencyList.add(bs);
        }

        BitSet[] classes = new BitSet[k];
        for (int i = 0; i < k; i++) {
            classes[i] = new BitSet(n);
        }

        List<Integer> vertices = Solution.randTraversalOrder(instance);
        for (int v : vertices) {
            boolean placed = false;
            for (int c = 1; c <= k; c++) {
                if (!adjacencyList.get(v).intersects(classes[c])) {
                    coloring[v] = c;
                    classes[c].set(v);
                    placed = true;
                    break;
                }
            }

            if (!placed) {
                coloring[v] = 0;
            }
        }
        return coloring;
    }

    public void init() {
        int obj = 0;        
        uncolored = new HashSet<Integer>();

        for (int i = 0; i < coloring.length; i++) {
            if (coloring[i] == 0) {
                obj++;
                uncolored.add(i);
            }
        }
        
        objective = obj;
    }

    public void calcNeighborObjective(Move move) {
        int obj = objective;
        if (coloring[move.getNode()] == 0 && move.getColor() != 0) {
            obj--;
            for (int adj : this.instance.getAdjacent(move.getNode())) {
                if (coloring[adj] == move.getColor()) {
                    obj++;
                }
            }
        } else if (move.getColor() == 0 && coloring[move.getNode()] != 0) {
            obj++;
        }

        move.setObjective(obj);
    }

    public void doMakeMove(Move move) {
        coloring[move.getNode()] = move.getColor();
        if (move.getColor() != 0) {
            for (int adj : this.instance.getAdjacent(move.getNode())) {
                if (coloring[adj] == move.getColor()) {
                    coloring[adj] = 0;
                }
            }
        }
        objective = move.getObjective();
    }

    public boolean isValidSolution() {
        return objective == 0;
    }   

}
