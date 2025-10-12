package general.SolutionClasses;

import java.util.*;

import general.*;
import general.HeuristicClasses.GCPHeuristic;

public class SolutionConflictCounts extends SolutionConflictObjective {
    // key is the node that is conflicted, and its value is the number of nodes that
    // it conflicts with
    protected HashMap<Integer, Integer> conflictCount;

    public SolutionConflictCounts(Instance instance, int[] coloring, int colors) {
        super(instance, coloring, colors);
    }

    public void init() {
        int obj = 0;
        conflictCount = new HashMap<Integer, Integer>();

        for (int i = 0; i < coloring.length; i++) {
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
    }

    public void makeMove(Move move) {
        int count = this.conflictCount.getOrDefault(move.getNode(), 0);
        int newColor = move.getColor();
        int oldColor = this.coloring[move.getNode()];
        for (int neighbor : instance.getAdjacent(move.getNode())) {
            if (this.coloring[neighbor] == newColor) {
                count++;
                this.conflictCount.put(neighbor, conflictCount.getOrDefault(neighbor, 0) + 1);
            } else if (this.coloring[neighbor] == oldColor) {
                count--;
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
        String str = "";
        for (int i = 0; i < coloring.length; i++) {
            str += "Node " + i + ": Color " + coloring[i] + "\n";
        }

        str += "\nObjective: " + getObjective() + "\nConflict List: ";

        for (int node : conflictCount.keySet()) {
            str += node + " ";
        }

        return str;
    }

}