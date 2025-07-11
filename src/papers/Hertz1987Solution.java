package papers;
import general.*;
import java.util.*;


public class Hertz1987Solution extends Solution {
    // constructor
    public Hertz1987Solution (Instance graph) {
        super(graph.getNumNodes(), graph, true, false);
    }
    
    // checks to see if a move is tabu based on the tabu map and the current iteration
    public boolean isTabu(HashMap<NodePair, Integer> tabuMap, NodePair move, int iteration, int tenure) {
        return tabuMap.containsKey(move) && iteration - tabuMap.get(move) <= tenure;
    }

    public NodePair generateBestRepNeighbor (int rep, HashMap<NodePair, Integer>tabuMap, int iteration, int tenure) {
        double bestObj = Integer.MAX_VALUE;
        NodePair bestMove = null;
        int i = 0; 
        while (i < rep) {
            NodePair currMove = generateRandomMove();
            double currObj = calcNeighborObjective(currMove); // O(n) operation
            if (!isTabu(tabuMap, currMove, iteration, tenure) || currObj < objective) {
                if (currObj < bestObj) {
                    bestObj = currObj;
                    bestMove = currMove;
                    if (bestObj < objective) {
                        break;
                    }
                }
                i += 1;
            }
        }
        objective = bestObj;
        return bestMove;
    }

    // this is the tabucol local search that will run
    public void tabuSearch (Heuristic heuristic, int tabuTenure, int rep) {
        // Initialization
        // hash map where the keys are node and coloring parings, value is where the pairing was made tabu
        HashMap<NodePair, Integer> tabuMap = new HashMap<>();
        int iteration = 0;

        while (objective > 0 && heuristic.report()) {
            NodePair neighbor = generateBestRepNeighbor(rep, tabuMap, iteration, tabuTenure);
            tabuMap.put(neighbor, iteration);
            coloring[neighbor.node] = neighbor.color;
            iteration++;
        }
    }

    // this will continue to loop the tabucol heuristic by reducing k by 1 everytime a valid k coloring is found
    // runs until the reachs its runtime limit (specified in the heurisitc object)
    public void Hertz1987 (Heuristic heuristic, int tabuTenure, int rep) {        
        while (heuristic.report()) {
            reduceK(); // start with reducing k because finding a k coloring where k = n is trivial
            tabuSearch(heuristic, tabuTenure, rep);
            printStatus();
        }
        
        System.out.println("runtime: " + heuristic.getCurrRunTime());
        printStatus();
    }

}