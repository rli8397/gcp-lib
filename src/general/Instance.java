package general;
import java.util.HashSet;
import java.io.File; 
import java.util.Scanner; 

public class Instance {
    private HashSet<Integer>[] adjacencySet; // adjacency matrix is boolean because it is non-directed and unweighted
    private int numNodes;
    private int maxChromatic;
    private int numEdges;
    @SuppressWarnings("unchecked")

    /* 
     * Constructor for an Intance that reads from a file
     * Format:
     * <number of nodes>
     * <edge1> <edge2>
     * ...
     * 
     * Ex:
     * 5 // this is the number of nodes
     * 0 1 // this is an edge between node 0 and node 1
     * 1 2
     * ... 
     * 
     */
    public Instance (File file) {
        try {
            Scanner scanner = new Scanner(file);
            int numNodes = scanner.nextInt();
            
            this.numNodes = numNodes;
            adjacencySet = new HashSet[numNodes + 1];
            for (int i = 0; i < adjacencySet.length; i++) {
                adjacencySet[i] = new HashSet<Integer>();
            }
            while (scanner.hasNextInt()) {
                int edge1 = scanner.nextInt();
                int edge2 = scanner.nextInt();
                addEdge(edge1, edge2);
            }
            scanner.close();

            //Calculate the maxchromatic number for the instance (maxdegree + 1)
            int maxDegree = 0;
            for (HashSet<Integer> adj : adjacencySet){
                if (maxDegree < adj.size()){
                    maxDegree = adj.size();
                }
            }

            maxChromatic = maxDegree + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this void method adds an edge between two given nodes
    public void addEdge(int edge1, int edge2) {
        if(adjacencySet[edge1].add(edge2)){
            numEdges += 1;
        }
        adjacencySet[edge2].add(edge1); // since the graph is undirected, we add both directions
    }

    // returns a set of size n + 1, the first element in the array is number of neighbors, followed by the neighbors themselves
    public HashSet<Integer> getAdjacent(int node) {
        return adjacencySet[node];
    }

    public boolean areAdjacent(int node1, int node2) {
        return adjacencySet[node1].contains(node2);
    }
    
    public int getNumNodes() {
        return numNodes;
    }

    public int getMaxChromatic(){
        return maxChromatic;
    }
    
    public int getNumEdges() {
        return numEdges;
    }

    public void printInstance() {
        for (int i = 0; i < adjacencySet.length; i++) {
            System.out.print("Node " + i + ": ");
            for (Integer neighbor : adjacencySet[i]) {
                System.out.print(neighbor + " ");
            }
            System.out.println();
        }
    }
    
}
