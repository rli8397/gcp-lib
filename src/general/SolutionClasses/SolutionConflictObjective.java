package general.SolutionClasses;

import java.util.ArrayList;
import java.util.HashSet;

import general.*;
import general.HeuristicClasses.GCPHeuristic;

public class SolutionConflictObjective extends Solution {
    public int objective;

    public SolutionConflictObjective(Instance instance, int[] coloring, int colors) {
        super(instance, coloring, colors);
    }

    public SolutionConflictObjective(SolutionConflictObjective other) {
        super(other);
        this.objective = other.objective;
    }

    // Counts number of conflicting edges and updates objective
    // O(n^2) - outside loop iterates through coloring which is len n inside loop
    // iterates through all adj nodes which is max n nodes
    public void init() {
        int obj = 0;
        for (int i = 1; i <= instance.getNumNodes(); i++) {
            HashSet<Integer> adj = this.instance.getAdjacent(i);
            for (int adjv : adj) {
                // If i < adjv, that edge hasn't been checked yet, this prevents from double
                // counting
                if (coloring[i] == coloring[adjv] && i < adjv) {
                    obj += 1;
                }
            }
        }
        objective = obj;
        System.out.println("Initial objective: " + objective);
    }

    // this calculates the objective function for a neigboring solution
    // O(n) - you must iterate through all adjacent nodes, which can be at most n
    // nodes
    public void calcNeighborObjective(Move move) {
        int obj = objective;
        for (int adj : this.instance.getAdjacent(move.getNode())) {
            if (coloring[adj] == coloring[move.getNode()]) {
                obj--;
            } else if (coloring[adj] == move.getColor()) {
                obj++;
            }
        }
        // if (obj > instance.getNumEdges()) {
        // System.out.println("\nError in calculating neighbor objective");
        // System.out.println(this);
        // System.out.println("Objective: " + obj);
        // System.out.println("Num Edges: " + instance.getNumEdges());
        // System.out.println("Move: " + move.getNode() + " to color " +
        // move.getColor());
        // }

        // if (obj == 5) {
        //     System.out.println("\nError in calculating neighbor objective but 8");
        //     System.out.println(this);
        //     System.out.println("Objective: " + obj);
        //     System.out.println("Num Edges: " + instance.getNumEdges());
        //     System.out.println("Move: " + move.getNode() + " to color " + move.getColor());
        // }
        move.setObjective(obj);
    }

    public void doMakeMove(Move move) {
        objective = move.getObjective();
        coloring[move.getNode()] = move.getColor();
    }

    // returns a random node that is adjacent to at least one node with the same
    // color. If there are no conflicted nodes, return -1
    // this can be used to determine functionality else where
    public int randConflictedNode() {
        if (objective == 0) {
            return -1;
        }

        ArrayList<Integer> conflictedNodes = new ArrayList<Integer>();
        for (int i = 1; i <= instance.getNumNodes(); i++) {
            for (int neighbor : this.instance.getAdjacent(i)) {
                if (coloring[i] == coloring[neighbor]) {
                    conflictedNodes.add(i);
                    break;
                }
            }
        }

        return conflictedNodes.get(GCPHeuristic.random(conflictedNodes.size()));
    }

    // returns a random move that involves a conflicted node
    // returns null if there are no conflicted nodes
    public Move randConflictedMove() {
        int node = randConflictedNode();

        if (node == -1) {
            return null;
        }

        int newColor = 0;
        do {
            newColor = GCPHeuristic.random(k) + 1;
        } while (newColor == coloring[node]);

        return new Move(node, newColor, this);
    }

    public boolean isValidSolution() {
        return objective == 0;
    }

    public int getObjective() {
        return objective;
    }

    // prints the current k the solution is checking and the best objective and
    // solution found so far
    // for debugging purposes
    public void printStatus() {
        System.out.println("\nk: " + k);
        System.out.println("f: " + objective);
        System.out.println(this);
    }

    public void validityCheck(int k) throws Exception {
        super.validityCheck(k);
        int obj = 0;
        for (int i = 1; i <= instance.getNumNodes(); i++) {
            HashSet<Integer> adj = this.instance.getAdjacent(i);
            for (int adjv : adj) {
                // If i < adjv, that edge hasn't been checked yet, this prevents from double
                // counting
                if (coloring[i] == coloring[adjv] && i < adjv) {
                    obj += 1;
                }
            }
        }

        if (objective != obj) {
            System.out.println(this);
            throw new Exception("Objective mismatch found! Expected: " + obj + ", Found: " + objective);
        }
    }

}
