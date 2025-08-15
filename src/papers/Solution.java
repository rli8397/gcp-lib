package papers;

import java.util.*;

import general.Instance;
import papers.Glass2003Heuristic.Glass2003Solution;
import general.Heuristic;

public abstract class Solution {
  protected int k;
  protected boolean validSolution;
  protected int[] coloring;
  protected Heuristic heuristic;
  protected Instance instance;

  // generates a random coloring array and returns it 
  protected static int[] randomColoring(int numNodes, int k, Random rand) {
    int[] coloring = new int[numNodes + 1];

    for (int i = 1; i < coloring.length; i++) {
      coloring[i] = rand.nextInt(k) + 1; // colors start from 1 to k
    }

    return coloring;
  }

  protected static int[] greedyConstruction(Instance instance, int k, Heuristic heuristic) {
      // Initialization
      HashSet<Integer>[] satDegree = new HashSet[instance.getNumNodes()];
      int[] coloring  = new int[instance.getNumNodes()];

      for (int i = 0; i < instance.getNumNodes(); i++) {
          satDegree[i] = new HashSet<Integer>();
      }

      for (int i = 0; i < instance.getNumNodes(); i++) {
          int minAllowed = Integer.MAX_VALUE;
          int currNode = -1;
          boolean[] usedColors = new boolean[k + 1];

          // loops through all nodes and searchs for the node with the minimum colors
          // allowed without giving penalty
          for (int node = 0; node < instance.getNumNodes(); node++) {
              if (coloring[node] == 0) { // Note: 0 means unvisited
                  int allowed = k - satDegree[node].size();
                  // -1 will notate that there is no allowed color
                  // these nodes will be randomly colored later
                  if (allowed == 0) {
                      coloring[node] = -1;
                  } else if (allowed < minAllowed) {
                      minAllowed = allowed;
                      currNode = node;
                  }
              }
          }

          // if no new node is able to be colored without penalty, break
          if (currNode == -1) {
              break;
          }

          // fills up an array denoted which colors are used by the current nodes
          // neighbors
          for (int neighbor : instance.getAdjacent(currNode)) {
              if (coloring[neighbor] > 0) {
                  usedColors[coloring[neighbor]] = true;
              }
          }

          // finds the smallest color class that is not used by the current nodes
          // neighbors
          int minColor = -1;
          for (int j = 1; j < usedColors.length; j++) {
              if (!usedColors[j]) {
                  minColor = j;
                  break;
              }
          }

          // currNode is assigned minColor, then updates the sat degree of all its
          // neighbors by adding the minColor to the set of colors neighboring the
          // neighbor
          coloring[currNode] = minColor;
          for (int neighbor : instance.getAdjacent(currNode)) {
              satDegree[neighbor].add(minColor);
          }


      }

      // randomly colors any nodes that couldn't be colored without penalty
      // (aka where this.coloring[i] == -1)
      for (int i = 0; i < instance.getNumNodes(); i++) {
          if (coloring[i] == -1) {
              coloring[i] = heuristic.random(k) + 1;
          }
      }

      return coloring;

  }
  
  public int randomNode() {
    return heuristic.random(instance.getNumNodes());
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

  public int calcK(){
        boolean[] visited = new boolean [this.k+1];

        int count = 0;
        for (int c : coloring){
            //Unique Color
            if (!visited[c]){
                visited[c] = true;
                count++;
            }
        }

        return count;

  }
}

