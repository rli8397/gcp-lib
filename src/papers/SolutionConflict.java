package papers;
import java.util.ArrayList;
import java.util.HashSet;

import general.Instance;

public class SolutionConflict extends Solution {
    protected int[] conflictCount;

    public SolutionConflict (int k, Instance g, boolean random, boolean stable){
        super(k, g, random, stable);
        conflictCount  = new int[g.getNumNodes()];
    }
    
    public SolutionConflict(SolutionConflict other){
        super(other);
        conflictCount = other.conflictCount;

    }

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
                    conflictCount[i] += 1;
                    conflictCount[adjv] +=1;
                }
            }
        }  
        } 
        objective  = obj;
    }

    public String toString(){
        String str = "";
        for (int i = 0; i < coloring.length; i++) {
        str += "Node " + i + ": Color " + coloring[i] + "\n";
        }

        str += "\nObjective: " + getObjective() + "\nConflict List: ";
        
        for (int i = 0; i < conflictCount.length; i++){
            str +=  conflictCount[i] + " ";
        }

        return str;
    }

    

<<<<<<< HEAD
     

=======
        ArrayList<Integer> indicies = new ArrayList<Integer>();
            for (int i = 0; i < conflictCount.length; i ++){
                if (conflictCount[i] > 0){
                    indicies.add(i);
                }
            }
            int random_node  = (int)(Math.random()*indicies.size());

            return indicies.get(random_node);
    }
>>>>>>> 7c8a517d7a958b271909a98b227900cfd7d8809c
    
}   
