import java.util.*;

public class Solution {
  private int k;
  private int[] coloring;
  private double objective;
  private Instance graph; 
  
  //Empty coloring
  public Solution (int colors, int n, Instance g, bool random, bool stable) {
    k = colors; //Must be >= 1 
    
    coloring =  new int[n]; 

    //-1 
    for (int i = 0; i < n; i++){

    graph = g;

    if (random){
      random_coloring();
    }
    else if (stable){
      stable_coloring();
    }

    objective = calcObjective();
  
  }
  
  //Copy Constructor, Deep Copy
  public Solution (Solution other){
    this.k = other.k;
    this.objective = other.objective;

    //Placeholder for instance class
    this.graph  = new Instance (other.graph);

    this.coloring = new int[other.coloring.length];
    
    //deep copy coloring 
    for (int i =0; i < other.coloring.length; i++){
      this.coloring[i] = other.coloring[i];
    }
  }

  //Splits graph into k sets 
  public void random_coloring(){
      Random randcol = new Random();

      //Ensures each color gets at least 1 assignment 1 to k
      for (int i = 1; i < k+1; i++){
        coloring[(int)(Math.random() * coloring.length)] = i;
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
      Hashset<Integer> adj = graph.getAdjacent(i);

      for (int adjv : adj){
         //If i < adjv, that edge hasn't been checked yet
         if (i < adjv){
            if (coloring[i] == coloring[adjv]){
                obj += 1;
            }
         }
       }  
    } 
    objective  = obj;
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
    System.out.println("Coloring" + coloring);
  }
  

  
}
