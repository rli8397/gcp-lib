package papers;

import java.util.*;

import general.Heuristic;
import general.Instance;

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
  
  public static ArrayList<Integer> randTraversalOrder(Instance instance) {
    ArrayList<Integer> order = new ArrayList<>();
    for (int i = 0; i < instance.getNumNodes(); i++) {
      order.add(i);
    }
    Collections.shuffle(order, Heuristic.getRandom());
    return order;
  }
  
  public int randomNode() {
    return Heuristic.random(instance.getNumNodes());
  }

  // this function decrements k then redisrupts the color that was previously the
  // kth color
  public abstract void reduceK();

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

