package papers;
import java.util.*;

import general.Instance;
import general.Heuristic;

// ADD A METHOD THAT UPDATES K WHEN SOLUTION CHANGES
public class Solution {
  protected int k;
  protected int[] coloring; // colors start from 1 to k, 0 indicates uncolored node
  protected double objective;
  protected Instance graph; 
  protected Heuristic heuristic;

  // this inner class allows a node color pair to be used as a hashable key
  public class Move {
      int node;
      int color;
      double objective = -1;

      public Move(int node, int color) {
          this.node = node;
          this.color = color; 
      }

      public boolean equals(Object obj) {
          if (this == obj) return true; 
          if (!(obj instanceof Move)) return false;
          Move other = (Move) obj;
          return node == other.node && color == other.color;
      }

      public int hashCode() {
          return Objects.hash(node, color);
      }
      
      public double getObjective() {
        if (objective == -1) {
          objective = calcNeighborObjective(this);
        }
        return objective;
      }

      public String toString() {
        return "Node: " + node + " Color: " + color + " Objective: " + getObjective();
      }
  }

  //Empty coloring
  public Solution (Heuristic heuristic, int colors, Instance graph, boolean random, boolean stable) {
    k = colors; //Must be >= 1 
    coloring =  new int[graph.getNumNodes()];
    this.graph = graph;
    this.heuristic = heuristic;

    if (random) {
      random_coloring();
    } else if (stable) {
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
    this.heuristic = other.heuristic; // this is wrong for a deepcopy but just a placeholder for now
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

  // this generates a random move from the current graph to a neighbor, doesn't modify the current graph yet
  public Move generateRandomMove() {
    Random rand = new Random();
    int node = rand.nextInt(graph.getNumNodes());
    int color = rand.nextInt(k) + 1; // add 1 since colors start from 1 to k
    
    // Note: this might not be the most random, it gives more probability to the color greater than the curr color
    if (coloring[node] == color) { // makes sure that the coloring is not the same color
        color += 1;
        if (color > k) {
            color = 1;
        }
    }

    return new Move(node, color);
  }

  // this calculates the objective function for a neigboring solution
  // O(n) - you must iterate through all adjacent nodes, which can be at most n nodes
  public double calcNeighborObjective (Move move) {
    double obj = objective;
    for (int adj : graph.getAdjacent(move.node)) {
      if (coloring[adj] == coloring[move.node]) {
        obj--;
      } else if (coloring[adj] == move.color) {
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

  //Picks a random node in the coloring
  public int random_node(){
    return (int)(Math.random() * coloring.length);
  }
   
  public void makeMove(Move move) {
    this.coloring[move.node] = move.color;
    this.objective = move.getObjective();
  }
  // this function decrements k then redisrupts the color that was previously the kth color
  // O(n^2) because of calcObjective()
  public void reduceK() {
    for (int i = 0; i < coloring.length; i++) {
      if (this.coloring[i] == k) {
        this.coloring[i] = (int) (Math.random() * (k - 1)) + 1;
      }
    }
    k--;
    calcObjective(); // maybe call make move instead of calcObjective
  }
  
  public int maxCardinality() {
    return 1;
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
  
  // prints the current k the solution is checking and the best objective and solution found so far
  public void printStatus() {
        System.out.println("k: " + k);
        System.out.println("f: " + objective);
        System.out.println(this);
  }

  // checks if two solutions are equal based on their coloring and their k value
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Solution)) return false;
    Solution solution = (Solution) o;
    return k == solution.k && Arrays.equals(coloring, solution.coloring);
  }
  
  
}
