package general.SolutionClasses;

import java.util.ArrayList;
import java.util.HashSet;

import general.*;
import general.HeuristicClasses.GCPHeuristic;

public class SolutionConflictObjective extends Solution {
    protected int objective;
    protected int nb_cfl;

    public SolutionConflictObjective(Instance instance, int[] coloring, int colors) {
        super(instance, coloring, colors);
    }

    // Counts number of conflicting edges and updates objective
    // O(n^2) - outside loop iterates through coloring which is len n inside loop
    // iterates through all adj nodes which is max n nodes
    public void init() {
        int obj = 0;
        for (int i = 0; i < coloring.length; i++) {
            HashSet<Integer> adj = this.instance.getAdjacent(i);
            boolean conflictedNode = false;
            for (int adjv : adj) {
                // If i < adjv, that edge hasn't been checked yet, this prevents from double
                // counting
                if (coloring[i] == coloring[adjv]) {
                    if (i < adjv) {
                        obj += 1;
                    }
                    if (!conflictedNode) {
                        nb_cfl++; 
                        conflictedNode = true;
                    }
                }
            }
        }
        objective = obj;
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

        move.setObjective(obj);
    }

    public void doMakeMove(Move move) {
        int node = move.getNode();
        int oldColor = this.coloring[node];
        int newColor = move.getColor();

        // Check if the moved node was previously conflicted
        boolean wasNodeConflicted = false;
        for (int neighbor : instance.getAdjacent(node)) {
            if (this.coloring[neighbor] == oldColor) {
                wasNodeConflicted = true;
                break;
            }
        }

        int newConflicts = 0;
        int oldConflicts = 0;
        int delta = 0; // Track neighbor conflicted status changes

        for (int neighbor : instance.getAdjacent(node)) {
            if (this.coloring[neighbor] == newColor) {
                // Will create a new conflict
                newConflicts++;

                // Check if this neighbor was previously unconflicted
                boolean wasNeighborConflicted = false;
                for (int n : instance.getAdjacent(neighbor)) {
                    if (n != node && this.coloring[n] == this.coloring[neighbor]) {
                        wasNeighborConflicted = true;
                        break;
                    }
                }
                if (!wasNeighborConflicted) {
                    delta++; // Neighbor became conflicted
                }

            } else if (this.coloring[neighbor] == oldColor) {
                // Removing an old conflict
                oldConflicts++;

                // Check if this neighbor will become unconflicted
                boolean willNeighborBeConflicted = false;
                for (int nn : instance.getAdjacent(neighbor)) {
                    if (nn != node && this.coloring[nn] == this.coloring[neighbor]) {
                        willNeighborBeConflicted = true;
                        break;
                    }
                }
                if (!willNeighborBeConflicted) {
                    delta--; // Neighbor became unconflicted
                }
            }
        }

        // Update nb_cfl based on neighbor status changes
        nb_cfl += delta;

        // Account for the moved node itself
        boolean isNodeConflicted = (newConflicts > 0);
        if (wasNodeConflicted && !isNodeConflicted) {
            nb_cfl--; // Node became unconflicted
        } else if (!wasNodeConflicted && isNodeConflicted) {
            nb_cfl++; // Node became conflicted
        }

        objective = move.getObjective();
        coloring[node] = newColor;
    }

    // returns a random node that is adjacent to at least one node with the same
    // color. If there are no conflicted nodes, return -1
    // this can be used to determine functionality else where
    public int randConflictedNode() {
        if (objective == 0) {
            return -1;
        }

        ArrayList<Integer> conflictedNodes = new ArrayList<Integer>();
        for (int i = 0; i < coloring.length; i++) {
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

    public int getNumConflictedNodes() {
        return nb_cfl;
    }

    // prints the current k the solution is checking and the best objective and
    // solution found so far
    // for debugging purposes
    public void printStatus() {
        System.out.println("k: " + k);
        System.out.println("f: " + objective);
        System.out.println(this);
    }
}
