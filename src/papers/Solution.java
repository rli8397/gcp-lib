package papers;

import java.util.*;

import general.Instance;
import general.Heuristic;
import papers.Move;

public abstract class Solution {
  protected int k;
  protected boolean validSolution;
  protected int[] coloring;
  protected Heuristic heuristic;
  protected Instance instance;

  // generates a random coloring array and returns it 
  protected static int[] randomColoring(int numNodes, int k, Random rand) {
    int[] coloring = new int[numNodes];

    for (int i = 0; i < coloring.length; i++) {
      coloring[i] = rand.nextInt(k) + 1; // colors start from 1 to k
    }

    return coloring;
  }

  public abstract void calcObjective();

  public abstract int calcNeighborObjective(Move move);

  public abstract void makeMove(Move move);

  public abstract Move generateRandomMove();
  
  public int randomNode() {
    return heuristic.random(instance.getNumNodes());
  }

  // this function decrements k then redisrupts the color that was previously the
  // kth color
  // O(n^2) because of calcObjective()
  public void reduceK() {
    for (int i = 0; i < coloring.length; i++) {
      if (this.coloring[i] == k) {
        this.coloring[i] = heuristic.random(k - 1) + 1; // reassigns the color to a random color from 1 to k-1
      }
    }
    k--;
    calcObjective(); // maybe call make move instead of calcObjective
  } 

  public int[] getColoring() {
    return this.coloring;
  }

  public int getK() {
    return this.k;
  }

  public boolean isValidSolution() {
    return validSolution;
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
}

