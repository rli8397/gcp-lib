package papers;

import java.util.HashSet;
import java.util.Random;

import general.Heuristic;

// maybe allow to be instantiated for testing purposes
public class SolutionConflictObjective extends Solution {
    protected int objective;

    public SolutionConflictObjective(Heuristic heuristic, int[] coloring, int colors) {
        this.heuristic = heuristic;
        this.instance = heuristic.getInstance();
        this.coloring = coloring;
        this.k = colors;
        calcObjective();
    }

    // Counts number of conflicting edges and updates objective
    // O(n^2) - outside loop iterates through coloring which is len n inside loop
    // iterates through all adj nodes which is max n nodes
    public void calcObjective() {
        int obj = 0;
        for (int i = 0; i < coloring.length; i++) {
            HashSet<Integer> adj = this.instance.getAdjacent(i);
            for (int adjv : adj) {
                // If i < adjv, that edge hasn't been checked yet, this prevents from double
                // counting
                if (i < adjv) {
                    if (coloring[i] == coloring[adjv]) {
                        obj += 1;
                    }
                }
            }
        }
        objective = obj;
        validSolution = objective == 0;
    }

    // this calculates the objective function for a neigboring solution
    // O(n) - you must iterate through all adjacent nodes, which can be at most n
    // nodes
    public int calcNeighborObjective(Move move) {
        int obj = objective;
        for (int adj : this.instance.getAdjacent(move.node)) {
            if (coloring[adj] == coloring[move.node]) {
                obj--;
            } else if (coloring[adj] == move.color) {
                obj++;
            }
        }

        return obj;
    }

    public void makeMove(Move move) {
        coloring[move.node] = move.color;
        objective = move.getObjective();
        validSolution = objective == 0;
    }

    // this generates a random move from the current instance to a neighbor, doesn't
    // modify the current instance yet
    public Move generateRandomMove() {
        Random rand = new Random();
        int node = rand.nextInt(this.instance.getNumNodes()) + 1;
        int color = rand.nextInt(k) + 1; // add 1 since colors start from 1 to k

        // Note: this might not be the most random, it gives more probability to the
        // color greater than the curr color
        if (coloring[node] == color) { // makes sure that the coloring is not the same color
            color += 1;
            if (color > k) {
                color = 1;
            }
        }

        return new Move(node, color, this);
    }

    public int getObjective() {
        return objective;
    }

    // prints the current k the solution is checking and the best objective and
    // solution found so far
    // for debugging purposes
    public void printStatus() {
        System.out.println("k: " + k);
        System.out.println("f: " + objective);
        System.out.println(this);
    }

    public int randConflictedNode() {
        int[] conflictedNodes = new int[coloring.length];
        int count = 0;
        for (int i = 0; i < coloring.length; i++) {
            for (int neighbor : this.instance.getAdjacent(i)) {
                if (coloring[i] == coloring[neighbor]) {
                    conflictedNodes[count] = i;
                    count++;
                    break;
                }
            }
        }

        if (count == 0) {
            return -1;
        }
        return conflictedNodes[Heuristic.random(count)];
    }

    public Move randConflictedMove() {
        int node = randConflictedNode();
        int newColor = 0;
        do {
            newColor = Heuristic.random(k) + 1;
        } while (newColor == coloring[node]);

        return new Move(node, newColor, this);
    }

    public Move randMove() {
        int node = Heuristic.random(coloring.length);
        int newColor = 0;
        do {
            newColor = Heuristic.random(k) + 1;
        } while (newColor == coloring[node] && k > 1);

        return new Move(node, newColor, this);
    }

    public void reduceK() {
        for (int i = 0; i < coloring.length; i++) {
            if (this.coloring[i] == k) {
                this.coloring[i] = Heuristic.random(k - 1) + 1; // reassigns the color to a random color from 1 to k-1
            }
        }
        k--;
        calcObjective();
    }
}
