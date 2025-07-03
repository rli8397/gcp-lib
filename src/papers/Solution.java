package papers;
import java.util.*;

import general.Instance;

// ADD A METHOD THAT UPDATES K WHEN SOLUTION CHANGES
public class Solution {
  protected int k;
  protected int[] coloring; // colors start from 1 to k, 0 indicates uncolored node
  protected double objective;
  protected Instance graph; 
  
  //Empty coloring
  public Solution (int colors, int n, Instance g, boolean random, boolean stable) {
    k = colors; //Must be >= 1 
    
    coloring =  new int[n]; 

    //-1 
    graph = g;
    if (random){
      random_coloring();
    }
    else if (stable){
      stable_coloring();
    }
    calcObjective();
  }
  
  public void stable_coloring() {

  }
 

  //Copy Constructor, Deep Copy
  public Solution (Solution other){
    this.k = other.k;
    this.objective = other.objective;

    //Placeholder for instance class
    this.graph = other.graph;

    this.coloring = new int[other.coloring.length];
    
    //deep copy coloring 
    for (int i =0; i < other.coloring.length; i++){
      this.coloring[i] = other.coloring[i];
    }
  }

  //Splits graph into k sets 
  public void random_coloring(){
     Random randcol = new Random();
      
      ArrayList<Integer> indicies = new ArrayList<>();
      for (int i = 0; i < coloring.length; i++) {
          indicies.add(i);
      }

      Collections.shuffle(indicies);
      
      for (int i = 1; i < k+1; i++){
        coloring[indicies.get(i-1)] = i;
      }

      //Partitions graph into k subsets
      for (int i = 0; i < coloring.length; i++){
        
        //Colors it from 1 to k if node is uncolored
        if (coloring[i] == 0)
          coloring[i] = randcol.nextInt(k+1) + 1; 
      }

      calcObjective();
  }

  //Counts number of conflicting edges and updates objective
  public void calcObjective(){
    int obj = 0;
    for (int i = 0; i < coloring.length; i++){
       
      //Placeholder
      HashSet<Integer> adj = graph.getAdjacent(i);
      for (int adjv : adj){
         //If i < adjv, that edge hasn't been checked yet
         if (i < adjv){
            if (coloring[i] == coloring[adjv]){
                obj += 1;
            }
         }
       }  
    } 
    objective = obj;
  } 

  //Makes one random move to generate new Neighbor
  public Solution generateNewNeighbor(){
    Solution neighbor  = new Solution (this);
    int index = (int)(Math.random()*coloring.length);
    int oldColor = neighbor.coloring[index];
    int newColor = 0;
    do{
      newColor = (int)(Math.random()*k) + 1;
    } while (newColor == oldColor);
    
    neighbor.coloring[index] = newColor;
    neighbor.calcObjective();
    return neighbor;
  }
   
// This function generates the best neighbor of the current solution
// If no neighbors are found, return null (will most likely never happen)
// Allows for neighbors that are worse than the current solution to be returned if it the best out of all neighbors
public Solution generateBestNeighbor(Condition condition) {
    int bestNode = -1;
    int bestColor = -1;
    double bestDelta = Double.POSITIVE_INFINITY;; // tracks improvement (negative = improvement), does allow generating worse neighbors

    // iterate through all nodes in the coloring (where coloring[node] is the color of node)
    for (int node = 0; node < coloring.length; node++) {
        int oldColor = coloring[node];
        
        // Calculate current conflicts for this node
        int currConflicts = 0;
        for (int adj : graph.getAdjacent(node)) {
            if (coloring[adj] == oldColor) {
                currConflicts++;
            }
        }
        
        // Try all other colors
        for (int newColor = 1; newColor <= k; newColor++) {
            if (newColor != oldColor) {
              // Calculate new conflicts for this node
              int newConflicts = 0;
              for (int adj : graph.getAdjacent(node)) {
                  if (coloring[adj] == newColor) {
                      newConflicts++;
                  }
              }
              
              double delta = newConflicts - currConflicts;
              // temporary changes this solution to check if it is a valid solution
              coloring[node] = newColor;
              objective += delta;
              if (delta < bestDelta && condition.isValid(this)) {
                  // update for the best neighbor found so far
                  bestDelta = delta;
                  bestNode = node;
                  bestColor = newColor;
              }

              // revert the temporary changes to this solution
              coloring[node] = oldColor;
              objective -= delta;
            }
            
        }
    }

    // If there are no neighbors return null(very unlikely)
    if (bestNode == -1) {
      return null;
    }

    // Apply the best move found
    Solution bestNeighbor = new Solution(this);
    bestNeighbor.coloring[bestNode] = bestColor;
    bestNeighbor.objective += bestDelta;
    return bestNeighbor;
}
  // this function decrements k then redisrupts the color that was previously the kth color
  public void reduceK() {
    for (int i = 0; i < coloring.length; i++) {
      if (coloring[i] == k) {
        coloring[i] = (int) (Math.random() * (k - 1)) + 1;
      }
    }
    k--;
    calcObjective();
  }

  //Accessors 
  public double getObjective(){ 
    return objective; 
  }
  
  public int[] getColoring(){
    return coloring;
  }

  //Print
  public String toString(){
    String str = "";
    for (int i = 0; i < coloring.length; i++) {
      str += "Node " + i + ": Color " + coloring[i] + "\n";
    }
    return str;
  }
  
  // checks if two solutions are equal based on their coloring and their k value
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Solution)) return false;
    Solution solution = (Solution) o;
    return k == solution.k && Arrays.equals(coloring, solution.coloring);
  }
  
}
