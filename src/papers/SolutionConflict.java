package papers;
import java.util.ArrayList;
import java.util.HashSet;

import general.Instance;

public class SolutionConflict extends Solution {
    protected int[] concount;

    public SolutionConflict (int k, Instance g, boolean random, boolean stable){
        concount  = new int[g.getNumNodes()];
        super(k, g, random, stable);

    }
    
    public SolutionConflict(SolutionConflict other){
        super(other);
        concount = other.concount;

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
                    concount[i] += 1;
                    concount[adjv] +=1;
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
        
        for (int i = 0; i < concount.length; i++){
            str +=  concount[i] + " ";
        }

        return str;
    }

    

     

    
}   
