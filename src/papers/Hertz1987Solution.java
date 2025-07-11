package papers;
import general.*;
import java.util.*;


public class Hertz1987Solution extends Solution {
    // this inner class allows a node color pair to be used as a hashable key
    public class NodePair {
        int node;
        int color;
        public NodePair(int node, int color) {
            this.node = node;
            this.color = color; 
        }

        public boolean equals(Object obj) {
            if (this == obj) return true; 
            if (!(obj instanceof NodePair)) return false;
            NodePair other = (NodePair) obj;
            return node == other.node && color == other.color;
        }

        public int hashCode() {
            return Objects.hash(node, color);
        }
    }

    public Hertz1987Solution (Instance graph) {
        super(graph.getNumNodes(), graph, true, false);
    }

    public void testing() {
        NodePair pair = new NodePair(1, 2);
        System.out.println(pair.node + " " + pair.color);
        HashMap<NodePair, Integer> map = new HashMap<>();
        map.put(pair, 1);
        map.put(pair, 2);
        map.put(new NodePair(1, 2), 0);
        map.put(new NodePair(3, 2), 5);

        NodePair notinmap = new NodePair(2,3);
        System.out.println(map.containsKey(pair));
        System.out.println(map.get(pair));
        for(NodePair i : map.keySet()){
            System.out.println(i.node + " "+ i.color);
        }
        System.out.println(isTabu(map, new NodePair(3,2), 10, 5));
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

            NodePair currMove = new NodePair(node, color);
            double currObj = calcNeighborObjective(node, color); // O(n) operation
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

    public void Hertz1987 (Heuristic heuristic, int tabuTenure, int rep) {
        // Initialization
        // hash map where the keys are node and coloring parings, value is where the pairing was made tabu
        HashMap<NodePair, Integer> tabuMap = new HashMap<>();
        int iteration = 0;
        
        while (heuristic.report()) {
            reduceK(); // start with reducing k because finding a k coloring where k = n is trivial
            while (objective > 0 && heuristic.report()) {
                // this section runs in O(rep * n), as checking for objective is O(n) which is run rep times, thus O(rep * n)
                NodePair neighbor = generateBestRepNeighbor(rep, tabuMap, iteration, tabuTenure);
                tabuMap.put(neighbor, iteration);
                coloring[neighbor.node] = neighbor.color;
                iteration++;
            }
            System.out.println("k: " + k);
            System.out.println("f: " + objective);
            System.out.println(this);
        }
        
        System.out.println("runtime: " + heuristic.getCurrRunTime());
        System.out.println("k: " + k);
        System.out.println("f: " + objective);
        System.out.println(this);
    }
}