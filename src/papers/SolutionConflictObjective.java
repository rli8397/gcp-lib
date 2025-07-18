package papers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;

import general.Heuristic;
import general.Instance;
import papers.Move;

public abstract class SolutionConflictObjective {
    protected int k;
    protected int objective;
    protected Instance instance;
    protected Heuristic heuristic;
    protected int[] coloring;
    protected Random rand = new Random();
    
    public abstract void randomColoring();

    public abstract void calcObjective();

    public abstract void makeMove(Move move);

    public abstract void reduceK();

    // Accessors
    public double getObjective() {
        return objective;
    }

    public T[] getColoring() {
        return coloring;
    }

    // prints the current k the solution is checking and the best objective and
    // solution found so far
    public void printStatus() {
        System.out.println("k: " + k);
        System.out.println("f: " + objective);
        System.out.println(this);
    }

    // Override toString()
    // Note this toString assumes that any type T is printable
    public String toString() {
        String str = "";
        for (int i = 0; i < coloring.length; i++) {
            str += "Node " + i + ": Color " + coloring[i] + "\n";
        }
        return str;
    }

    // checks if two solutions are equal based on their coloring and their k value
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Solution2))
            return false;
        Solution2<T> solution = (Solution2<T>) o;
        return k == solution.k && Arrays.equals(coloring, solution.coloring);
    }
}
