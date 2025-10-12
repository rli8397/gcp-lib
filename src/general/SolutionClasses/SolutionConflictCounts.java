package general.SolutionClasses;

import java.util.*;

import general.*;
import general.HeuristicClasses.GCPHeuristic;

public class SolutionConflictCounts extends SolutionConflictObjective {
    // key is the node that is conflicted, and its value is the number of nodes that it conflicts with
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
                if (i < adjv) {
                    if (this.coloring[i] == this.coloring[adjv]) {
                        obj += 1;
                        this.conflictCount.put(i, conflictCount.getOrDefault(i, 0) + 1);
                        this.conflictCount.put(adjv, conflictCount.getOrDefault(adjv, 0) + 1);
                    }
                }
            }
        }
        objective = obj;
    }

    public void makeMove(Move move) {
        int newConflicts = 0;
        int oldConflicts = 0;
        for (int neighbor : instance.getAdjacent(move.getNode())) {
            if (this.coloring[neighbor] == move.getColor()) {
                newConflicts++;
                this.conflictCount.put(neighbor, conflictCount.getOrDefault(neighbor, 0) + 1);
            } else if (this.coloring[neighbor] == this.coloring[move.getNode()]) {
                oldConflicts++;
                int neighborConflicts = this.conflictCount.get(neighbor);

                if (neighborConflicts <= 1) {
                    this.conflictCount.remove(neighbor);
                } else {
                    this.conflictCount.put(neighbor, neighborConflicts - 1);
                }

            }
        }
        coloring[move.getNode()] = move.getColor();
        nb_cfl += newConflicts - oldConflicts;

        // if there are no oldConflicts, then this node was previously unconflicted
        // meaning that you need to account for this node being newly conflicted
        if (oldConflicts == 0) {
            nb_cfl++;
        }

        // if there are no new conflicts, then this node is newly unconflicted
        if (newConflicts == 0) {
            nb_cfl--;
        }

        objective = move.getObjective();
    }

    // Picks a random node with conflicts, deviation from picking ANY random node
    public int randConflictedNode() {
        if (conflictCount.size() == 0) {
            return -1;
        }
        ArrayList<Integer> indicies = new ArrayList<Integer>(conflictCount.keySet());
        // for (int node : conflictCount.keySet()) {
        //     indicies.add(node);
        // }

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