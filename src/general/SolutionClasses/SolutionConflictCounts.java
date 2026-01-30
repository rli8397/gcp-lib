package general.SolutionClasses;

import java.util.*;

import general.*;
import general.HeuristicClasses.GCPHeuristic;

public class SolutionConflictCounts extends SolutionConflictObjective {
    // key is the node that is conflicted, and its value is the number of nodes that
    // it conflicts with
    protected HashMap<Integer, Integer> conflictCount;
    protected int nb_cfl;

    public SolutionConflictCounts(Instance instance, int[] coloring, int colors) {
        super(instance, coloring, colors);
    }

    public SolutionConflictCounts(SolutionConflictCounts other) {
        super(other);
        this.conflictCount = new HashMap<>(other.conflictCount);
        this.nb_cfl = other.nb_cfl;
    }

    public void init() {
        int obj = 0;
        conflictCount = new HashMap<Integer, Integer>();

        for (int i = 1; i <= instance.getNumNodes(); i++) {
            // Placeholder
            HashSet<Integer> adj = this.instance.getAdjacent(i);

            for (int adjv : adj) {
                // If i < adjv, that edge hasn't been checked yet
                if (i < adjv && this.coloring[i] == this.coloring[adjv]) {
                    obj += 1;
                    this.conflictCount.put(i, conflictCount.getOrDefault(i, 0) + 1);
                    this.conflictCount.put(adjv, conflictCount.getOrDefault(adjv, 0) + 1);
                }
            }
        }
        objective = obj;
        nb_cfl = conflictCount.size();
    }

    public void doMakeMove(Move move) {
        int count = this.conflictCount.getOrDefault(move.getNode(), 0);
        int newColor = move.getColor();
        int oldColor = this.coloring[move.getNode()];
        for (int neighbor : instance.getAdjacent(move.getNode())) {
            if (this.coloring[neighbor] == newColor) {
                count++;
                this.conflictCount.put(neighbor, conflictCount.getOrDefault(neighbor, 0) + 1);
            } else if (this.coloring[neighbor] == oldColor) {
                count--;
                // issue: even though neighbor is supposed to be conflicted here, its not in the
                // hashmap
                int neighborConflicts = this.conflictCount.get(neighbor);
                if (neighborConflicts <= 1) {
                    this.conflictCount.remove(neighbor);
                } else {
                    this.conflictCount.put(neighbor, neighborConflicts - 1);
                }

            }
        }

        this.conflictCount.put(move.getNode(), count);
        if (count <= 0) {
            this.conflictCount.remove(move.getNode());
        }
        objective = move.getObjective();
        nb_cfl = conflictCount.size();
        coloring[move.getNode()] = move.getColor();
    }

    // Picks a random node with conflicts, deviation from picking ANY random node
    public int randConflictedNode() {
        if (conflictCount.size() == 0) {
            return -1;
        }
        ArrayList<Integer> indicies = new ArrayList<Integer>(conflictCount.keySet());

        int random_node = GCPHeuristic.random(indicies.size());

        return indicies.get(random_node);
    }

    public String toString() {
        System.out.println("\nPrint Start:");
        String str = "";
        for (int i = 1; i <= instance.getNumNodes(); i++) {
            str += "Node " + i + ": Color " + coloring[i] + "\n";
        }

        str += "\nObjective: " + getObjective() + "\nConflict List: ";

        for (int node : conflictCount.keySet()) {
            str += node + " ";
        }
        return str;
    }

    public void validityCheck(int k) throws Exception {
        super.validityCheck(k);
        
        // Recalculate conflictCount based on current coloring
        HashMap<Integer, Integer> expectedConflictCount = new HashMap<>();
        for (int i = 1; i <= instance.getNumNodes(); i++) {
            HashSet<Integer> adj = this.instance.getAdjacent(i);
            int count = 0;
            for (int adjv : adj) {
                if (coloring[i] == coloring[adjv]) {
                    count++;
                }
            }
            if (count > 0) {
                expectedConflictCount.put(i, count);
            }
        }
        
        // Check if conflictCount matches expectedConflictCount
        if (!conflictCount.equals(expectedConflictCount)) {
            StringBuilder msg = new StringBuilder();
            msg.append("VALIDITY ERROR: conflictCount mismatch\n");
            msg.append("Expected: ").append(expectedConflictCount).append("\n");
            msg.append("Got: ").append(conflictCount).append("\n");
            
            // Find differences
            for (int node : expectedConflictCount.keySet()) {
                if (!conflictCount.containsKey(node)) {
                    msg.append("Missing node ").append(node).append(" with count ").append(expectedConflictCount.get(node)).append("\n");
                } else if (!conflictCount.get(node).equals(expectedConflictCount.get(node))) {
                    msg.append("Node ").append(node).append(": expected ").append(expectedConflictCount.get(node))
                        .append(", got ").append(conflictCount.get(node)).append("\n");
                }
            }
            for (int node : conflictCount.keySet()) {
                if (!expectedConflictCount.containsKey(node)) {
                    msg.append("Extra node ").append(node).append(" with count ").append(conflictCount.get(node)).append("\n");
                }
            }
            
            throw new Exception(msg.toString());
        }
        
        // Check if nb_cfl matches the size of conflictCount
        if (nb_cfl != conflictCount.size()) {
            throw new Exception("VALIDITY ERROR: nb_cfl mismatch. Expected " + conflictCount.size() + ", got " + nb_cfl);
        }
    }
}