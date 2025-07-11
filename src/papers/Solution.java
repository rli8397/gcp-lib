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
  public Solution (int colors, Instance g, boolean random, boolean stable) {
    k = colors; //Must be >= 1 
    
    coloring =  new int[g.getNumNodes()]; 

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
  // O(n^2) - outside loop iterates through coloring which is len n inside loop
  // iterates through all adj nodes which is max n nodes 
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

  // this calculates the objective function for a neigboring solution
  // O(n) - you must iterate through all adjacent nodes, which can be at most n nodes
  public double calcNeighborObjective (int node, int newColor) {
    double obj = objective;
    for (int adj : graph.getAdjacent(node)) {
      if (coloring[adj] == coloring[node]) {
        obj--;
      } else if (coloring[adj] == newColor) {
        obj++;
      }
    }

    return obj;
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
<<<<<<< HEAD

  //Picks a random node in the coloring
  public int random_node(){
    return (int)(Math.random() * coloring.length);
  }
  
  // this function decrements k then redisrupts the color that was previously the kth color
  // O(n^2) because of calcObjective()
  public void reduceK() {
    for (int i = 0; i < coloring.length; i++) {
      if (coloring[i] == k) {
        coloring[i] = (int) (Math.random() * (k - 1)) + 1;
      }
    }
    k--;
    calcObjective();
  }
=======
   
  // this function decrements k then redisrupts the color that was previously the kth color
  // O(n^2) because of calcObjective()
  public void reduceK() {
    for (int i = 0; i < coloring.length; i++) {
      if (coloring[i] == k) {
        coloring[i] = (int) (Math.random() * (k - 1)) + 1;
      }
    }
    k--;
    calcObjective();
  }
>>>>>>> 745d60e2bd2ab37328db0ad2b8557b398d2734e9

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
<<<<<<< HEAD

=======
>>>>>>> 745d60e2bd2ab37328db0ad2b8557b398d2734e9
  
  
}
