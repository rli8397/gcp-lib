package papers;
import general.*;
import java.util.*;

public class Hertz1987Solution extends Solution {
    public Hertz1987Solution (Instance graph) {
        super(graph.getNumNodes(), graph.getNumNodes(), graph, true, false);
    }

    public void Hertz1987 (Heuristic heuristic, int tabuTenure) {
        // Initialization
        Queue<Solution> tabuList = new LinkedList<>();
        while (heuristic.report()) {
            while (objective > 0 && heuristic.report()) {
                Solution bestNeighbor = this.generateBestNeighbor(new TabuCondition(tabuList, objective));
                
                tabuList.add(bestNeighbor);
                if (tabuList.size() > tabuTenure) {
                    tabuList.remove();
                }

                if(bestNeighbor.objective < objective) {
                    coloring = bestNeighbor.coloring;
                    objective = bestNeighbor.objective;
                }
            }
            reduceK();
        }
        
        System.out.println("runtime: " + heuristic.getCurrRunTime());
        System.out.println("f: " + objective);
    }
}