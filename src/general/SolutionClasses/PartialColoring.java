package general.SolutionClasses;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;

import general.Instance;
import general.Move;
import general.HeuristicClasses.*;

public class PartialColoring extends Solution {
    protected HashSet<Integer> uncolored;
    
    public PartialColoring(Instance instance, int[] coloring, int k) {
        super(instance, coloring, k);
        init(); // sets objective and uncolored set
    }

    public PartialColoring(PartialColoring other) {
        super(other.instance, other.coloring.clone(), other.k);
        this.uncolored = new HashSet<>(other.uncolored);
    }

    // according to blochilger2008
    public static int[] partialColoring(GCPHeuristic heuristic, int k) {
        Instance instance = heuristic.getInstance();
        int n = instance.getNumNodes();
        int[] coloring = new int[n + 1];

        // convert adjancey set to bitset form
        List<BitSet> adjacencyList = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            BitSet bs = new BitSet(n + 1);
            for (int neighbor : instance.getAdjacent(i)) {
                bs.set(neighbor);
            }
            adjacencyList.add(bs);
        }

        BitSet[] classes = new BitSet[k + 1];
        for (int i = 1; i <= k; i++) {
            classes[i] = new BitSet(n + 1);
        }

        // choose verticies in random order and assign the smallest color class possible 
        // while maintaining stable sets (non-conflicted)
        List<Integer> vertices = Solution.randTraversalOrder(instance);
        for (int v : vertices) {
            boolean placed = false;
            for (int c = 1; c <= k; c++) {
                if (!adjacencyList.get(v - 1).intersects(classes[c])) {
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
        uncolored = new HashSet<Integer>();
        for (int i = 1; i < coloring.length; i++) {
            if (coloring[i] == 0) {
                uncolored.add(i);
            }
        }
    }

    public void calcNeighborObjective(Move move) {
        int obj = uncolored.size();
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
            uncolored.remove(move.getNode());
            for (int adj : this.instance.getAdjacent(move.getNode())) {
                if (coloring[adj] == move.getColor()) {
                    coloring[adj] = 0;
                    uncolored.add(adj);
                }
            }
        } else {
            uncolored.add(move.getNode());
        }
    }

    public boolean isValidSolution() {
        return uncolored.size() == 0;
    }   

    public int getObjective() {
        return uncolored.size();
    }

}
