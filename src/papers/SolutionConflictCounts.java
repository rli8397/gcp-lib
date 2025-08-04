package papers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import general.Heuristic;
import general.Instance;

public class SolutionConflictCounts extends SolutionConflictObjective {
    protected int[] conflictCount;

    public SolutionConflictCounts(Heuristic heuristic, int [] coloring, int colors) {
        super(heuristic, coloring, colors);
        conflictCount = new int[instance.getNumNodes() + 1];
    }

    public void calcObjective() {
        int obj = 0;
        conflictCount = new int[instance.getNumNodes() + 1];
        for (int i = 0; i < this.coloring.length; i++) {
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
        this.objective = obj;
        this.validSolution = objective == 0;
    }

    public void makeMove(Move move) {
        for (int i : this.instance.getAdjacent(move.node)) {
            if (this.coloring[i] == move.color) { // if the coloring of the neighbor == the new color, add a conflict
                this.conflictCount[move.node]++;
                this.conflictCount[i]++;
            } else if (coloring[i] == coloring[move.node]) { // if the coloring of the neighbor == the color of the current color of the node, subtract a conflict
                this.conflictCount[move.node]--;
                this.conflictCount[i]--;
            }
        }
        this.coloring[move.node] = move.color;
        this.objective = move.getObjective();
        this.validSolution = this.objective == 0;
    }

    // Picks a random node with conflicts, deviation from picking ANY random node
    public int randConflictedNode() {
        ArrayList<Integer> indicies = new ArrayList<Integer>();
        for (int i = 0; i < conflictCount.length; i++) {
            if (conflictCount[i] > 0) {
                indicies.add(i);
            }
        }
        
        int random_node = this.heuristic.random(indicies.size());

        return indicies.get(random_node);
    }

    public Move randConflictedMove() {
        if (k <= 1) {
            return new Move(1, 1, this);
        }
        int node = randConflictedNode();
        int newColor = 0;
        do {
            newColor = heuristic.random(this.k) + 1;
        } while (newColor == coloring[node]);

        return new Move(node, newColor, this);
    }

    public String toString() {
        String str = "";
        for (int i = 1; i < coloring.length; i++) {
            str += "Node " + i + ": Color " + coloring[i] + "\n";
        }

        str += "\nObjective: " + getObjective() + "\nConflict List: ";

        for (int i = 1; i < conflictCount.length; i++) {
            str += conflictCount[i] + " ";
        }

        return str;
    }

}