package papers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import general.Heuristic;
import general.Instance;

public class PartialColoring extends Solution {
    protected int objective; // objective is the number of uncolored nodes

    public PartialColoring(Heuristic heuristic, int[] coloring, int k) {
        this.heuristic = heuristic;
        this.instance = heuristic.getInstance();
        this.coloring = coloring;
        this.k = k;
    }

    protected static int[] partialColoring(Instance instance, int k, Random rand) {
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

    public void calcObjective() {
        int obj = 0;
        for (int i = 0; i < coloring.length; i++) { 
            if (coloring[i] == 0) {
                obj++;
            }
        }
        objective = obj;
    }

    public void calcNeighborObjective(Move move) {
        int obj = objective;
        if (coloring[move.node] == 0 && move.color != 0) {
            obj--;
            for (int adj : this.instance.getAdjacent(move.node)) {
                if (coloring[adj] == move.color) {
                    obj++;
                }
            }
        } else if (move.color == 0 && coloring[move.node] != 0) {
            obj++;
        } 

        move.setObjective(obj);
    }

    public void makeMove(Move move){
        coloring[move.node] = move.color;
        if (move.color != 0) {
            for (int adj : this.instance.getAdjacent(move.node)) {
                if (coloring[adj] == move.color) {
                    coloring[adj] = 0; 
                }
            }
        }
        objective = move.getObjective();
        validSolution = objective == 0;
    }

}
