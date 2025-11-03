package general.SolutionClasses;

import java.util.*;

import general.Instance;
import general.Move;
import general.HeuristicClasses.GCPHeuristic;

public abstract class Solution {
  protected int k;
  protected int[] coloring;
  protected Instance instance;

  public Solution(Instance instance, int[] coloring, int k) {
    this.instance = instance;
    this.coloring = coloring;
    this.k = k;
    init();
  }

  // generates a random coloring array and returns it
  public static int[] randomColoring(Instance instance, int k) {
    int[] coloring = new int[instance.getNumNodes()];

    for (int i = 0; i < coloring.length; i++) {
      coloring[i] = GCPHeuristic.random(k) + 1; // colors start from 1 to k
    }

    return coloring;
  }


  public static ArrayList<Integer> randTraversalOrder(Instance instance) {
    ArrayList<Integer> order = new ArrayList<>();
    for (int i = 0; i < instance.getNumNodes(); i++) {
      order.add(i);
    }
    Collections.shuffle(order, GCPHeuristic.getRandom());
    return order;
  }

  public int randomNode() {
    return GCPHeuristic.random(instance.getNumNodes());
  }

  public abstract void init();

  public abstract void calcNeighborObjective(Move move);
  
  public abstract boolean isValidSolution();

  // public abstract Move randConflictedMove();

  /*
    * This makes the given move to the current coloring. 
    * A move is considered as changing the color of a node to a new color
    * After updating the coloring, it also updates the objective function
    * 
    * Subclasses implement doMakeMove for specifiy their own functionalities
    * 
    * Break case:
    *  If a useless move is passed in (changing a node's color to the same color), 
    *  the function returns immediately
    */

  public void makeMove(Move move) {
    if (this.coloring[move.getNode()] == move.getColor()) {
      return; 
    }

    doMakeMove(move);
  }

  public abstract void doMakeMove(Move move);

  public Move randMove() {
    int node = GCPHeuristic.random(coloring.length);
    int newColor = 0;
    do {
      newColor = GCPHeuristic.random(k) + 1;
    } while (newColor == coloring[node] && k > 1);

    return new Move(node, newColor, this);
  }

  public int[] getColoring() {
    return this.coloring;
  }

  public int getNodeColor(int node) {
    return this.coloring[node];
  }
  
  public int getK() {
    return this.k;
  }

  public Instance getInstance() {
    return this.instance;
  }

  public String toString() {
    String str = "";
    for (int i = 0; i < coloring.length; i++) {
      str += "Node " + i + ": Color " + coloring[i] + "\n";
    }
    return str;
  }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Solution))
      return false;
    Solution solution = (Solution) o;
    return k == solution.k && Arrays.equals(coloring, solution.coloring);
  }

  public int calcK() {
    boolean[] visited = new boolean[this.k + 1];

    int count = 0;
    for (int c : coloring) {
      // Unique Color
      if (!visited[c]) {
        visited[c] = true;
        count++;
      }
    }

    return count;

  }
}
