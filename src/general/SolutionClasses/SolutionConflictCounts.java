package general.SolutionClasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import general.Instance;
import general.Move;
import general.HeuristicClasses.GCPHeuristic;

public class SolutionConflictCounts extends SolutionConflictObjective {
    // key is the node that is conflicted, and its value is the number of nodes that
    // it conflicts with
    protected HashMap<Integer, Integer> conflictCount;
    protected boolean conflictedNodesObjective = false;

    public SolutionConflictCounts(Instance instance, int[] coloring, int colors) {
        super(instance, coloring, colors);
    }

    public SolutionConflictCounts(Instance instance, int[] coloring, int colors, boolean conflictedNodesObjective) {
        super(instance, coloring, colors);
        this.conflictedNodesObjective = conflictedNodesObjective;
    }

    public SolutionConflictCounts(SolutionConflictCounts other) {
        super(other);
        this.conflictCount = new HashMap<Integer, Integer>(other.conflictCount);
        this.conflictedNodesObjective = other.conflictedNodesObjective;
    }
    
    public void init() {
        int obj = 0;
        conflictCount = new HashMap<Integer, Integer>();

        for (int i = 0; i < coloring.length; i++) {
            HashSet<Integer> adj = this.instance.getAdjacent(i);

            for (int adjv : adj) {
                // If i < adjv, that edge hasn't been checked yet, this prevents from double
                // counting
                if (coloring[i] == coloring[adjv] && i < adjv) {
                    obj += 1;
                    this.conflictCount.put(i, conflictCount.getOrDefault(i, 0) + 1);
                    this.conflictCount.put(adjv, conflictCount.getOrDefault(adjv, 0) + 1);
                }
            }
        }

        objective = obj;
    }

    public int getConflictedNodeCount() {
        return conflictCount == null ? 0 : conflictCount.size();
    }

    public boolean nodeIsConflicted(int node) {
        return conflictCount.containsKey(node);
    }

    public Set<Integer> getConflictedNodes() {
        return conflictCount.keySet();
    }
    // Counts number of conflicting edges and updates objective
    public void calcNeighborObjective(Move move) {
        if (conflictedNodesObjective) {
            move.setObjective(getConflictedNodeCountAfterMove(move));
            return;
        }

        int obj = objective;
        for (int adj : this.instance.getAdjacent(move.getNode())) {
            if (coloring[adj] == coloring[move.getNode()]) {
                obj--;
            } else if (coloring[adj] == move.getColor()) {
                obj++;
            }
        }

        move.setObjective(obj);
    }

    public void doMakeMove(Move move) {
        int obj = objective;
        int count = this.conflictCount.getOrDefault(move.getNode(), 0);
        int oldColor = this.coloring[move.getNode()];
        int newColor = move.getColor();
        for (int neighbor : instance.getAdjacent(move.getNode())) {
            if (this.coloring[neighbor] == newColor) {
                obj++;
                count++;
                this.conflictCount.put(neighbor, conflictCount.getOrDefault(neighbor, 0) + 1);
            } else if (this.coloring[neighbor] == oldColor) {
                obj--;
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
        objective = obj;
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
        for (int i = 0; i < instance.getNumNodes(); i++) {
            str += "Node " + i + ": Color " + coloring[i] + "\n";
        }

        str += "\nObjective: " + getObjective() + "\nConflict List: ";

        for (int node : conflictCount.keySet()) {
            str += node + " ";
        }
        return str;
    }

    private int getConflictedNodeCountAfterMove(Move move) {
        int node = move.getNode();
        int oldColor = this.coloring[node];
        int newColor = move.getColor();

        int conflictedNodes = getConflictedNodeCount();
        int delta = 0;

        int oldNodeCount = this.conflictCount.getOrDefault(node, 0);
        int newNodeCount = 0;
        for (int neighbor : this.instance.getAdjacent(node)) {
            if (this.coloring[neighbor] == newColor) {
                newNodeCount++;
            }
        }
        delta += (newNodeCount > 0 ? 1 : 0) - (oldNodeCount > 0 ? 1 : 0);

        for (int neighbor : this.instance.getAdjacent(node)) {
            int oldCount = this.conflictCount.getOrDefault(neighbor, 0);
            int newCount = oldCount;
            if (this.coloring[neighbor] == oldColor) {
                newCount--;
            } else if (this.coloring[neighbor] == newColor) {
                newCount++;
            }
            delta += (newCount > 0 ? 1 : 0) - (oldCount > 0 ? 1 : 0);
        }

        return conflictedNodes + delta;
    }

    public void validityCheck(int k) throws Exception {
        super.validityCheck(k);
        
        // Recalculate conflictCount based on current coloring
        HashMap<Integer, Integer> expectedConflictCount = new HashMap<>();
        for (int i = 0; i < instance.getNumNodes(); i++) {
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
        if (expectedConflictCount.size() != conflictCount.size()) {
            throw new Exception("VALIDITY ERROR: nb_cfl mismatch. Expected " + conflictCount.size() + ", got " + expectedConflictCount.size());
        }
    }
}
