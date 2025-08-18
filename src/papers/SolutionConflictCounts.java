package papers;

import java.util.ArrayList;
import java.util.HashSet;

import general.Heuristic;

public class SolutionConflictCounts extends SolutionConflictObjective {
    protected int[] conflictCount;

    public SolutionConflictCounts(Heuristic heuristic, int [] coloring, int colors) {
        super(heuristic, coloring, colors);
    }

    public void calcObjective() {
        int obj = 0;
        conflictCount = new int[instance.getNumNodes()];

        for (int i = 0; i < coloring.length; i++) {
            // Placeholder
            HashSet<Integer> adj = this.instance.getAdjacent(i);

            for (int adjv : adj) {
                // If i < adjv, that edge hasn't been checked yet
                if (i < adjv) {
                    if (this.coloring[i] == this.coloring[adjv]) {
                        obj += 1;
                        this.conflictCount[i] += 1;
                        this.conflictCount[adjv] += 1;
                    }
                }
            }
        }
        objective = obj;
        this.validSolution = objective == 0;
    }

    // Picks a random node with conflicts, deviation from picking ANY random node
    public int randConflictedNode() {
        ArrayList<Integer> indicies = new ArrayList<Integer>();
        for (int i = 0; i < conflictCount.length; i++) {
            if (conflictCount[i] > 0) {
                indicies.add(i);
            }
        }

        int random_node = Heuristic.random(indicies.size());

        return indicies.get(random_node);
    }

    public Move randConflictedMove() {
        if (k <= 1) {
            return new Move(1, 1, this);
        }
        int node = randConflictedNode();
        int newColor = 0;
        do {
            newColor = Heuristic.random(this.k) + 1;
        } while (newColor == coloring[node]);

        return new Move(node, newColor, this);
    }

    public String toString() {
        String str = "";
        for (int i = 0; i < coloring.length; i++) {
            str += "Node " + i + ": Color " + coloring[i] + "\n";
        }

        str += "\nObjective: " + getObjective() + "\nConflict List: ";

        for (int i = 0; i < conflictCount.length; i++) {
            str += conflictCount[i] + " ";
        }

        return str;
    }

}