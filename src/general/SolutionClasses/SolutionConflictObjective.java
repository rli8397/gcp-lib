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
                        nb_cfl++; // maybe consider making conflict count solution count the conflict vertices
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

    public void makeMove(Move move) {
        int newConflicts = 0;
        int oldConflicts = 0;
        for (int neighbor : instance.getAdjacent(move.getNode())) {
            if (this.coloring[neighbor] == move.getColor()) {
                newConflicts++;
            } else if (this.coloring[neighbor] == this.coloring[move.getNode()]) {
                oldConflicts++;
            }
        }
        
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
        coloring[move.getNode()] = move.getColor();

    }

    // returns a random node that is adjacent to at least one node with the same color
    // if there are no node conflicts, returns -1
    public int randConflictedNode() {
        ArrayList<Integer> conflictedNodes = new ArrayList<Integer>();
        for (int i = 0; i < coloring.length; i++) {
            for (int neighbor : this.instance.getAdjacent(i)) {
                if (coloring[i] == coloring[neighbor]) {
                    conflictedNodes.add(i);
                    break;
                }
            }
        }

        if (conflictedNodes.size() == 0) {
            return -1;
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
