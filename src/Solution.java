import java.util.*;

public class Solution {
  private int k;
  private int[] coloring;
  private double objective;
  private Instance graph; 
  
  //Empty coloring
  public Solution (int colors, int n, Instance g, boolean random, boolean stable) {
    k = colors; //Must be >= 1 
    
    coloring =  new int[n]; 
<<<<<<< HEAD
=======

    //-1 
>>>>>>> 644dbbb9ddb239b28b17a66305621cd84f42b65a
    graph = g;
    if (random){
      random_coloring();
    }
    else if (stable){
      stable_coloring();
    }
<<<<<<< HEAD

    calcObjective();
  
=======
    calcObjective();
>>>>>>> 644dbbb9ddb239b28b17a66305621cd84f42b65a
  }
  
  public void stable_coloring() {

  }

  //Copy Constructor, Deep Copy
  public Solution (Solution other){
    this.k = other.k;
    this.objective = other.objective;

    //Placeholder for instance class
<<<<<<< HEAD
    this.graph  = other.graph;
=======
    this.graph = other.graph;
>>>>>>> 644dbbb9ddb239b28b17a66305621cd84f42b65a

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

  public void stable_coloring(){
      return;
  }

  //Counts number of conflicting edges and updates objective
  public void calcObjective(){
    double obj = 0;
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
    }while (newColor == oldColor);
    
    neighbor.vertexChange(index, newColor);
    return neighbor;
  }

  //index 0 to n-1, color 1 to k
  public void vertexChange(int index, int color){
    coloring[index] = color;
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
    return "Coloring: " + coloring;
  }
  

  
}
