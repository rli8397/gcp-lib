package general;
import java.util.HashSet;
import java.io.File; 
import java.util.Scanner; 
import java.util.*;

public class Instance {
    private HashSet<Integer>[] adjacencySet; // adjacency matrix is boolean because it is non-directed and unweighted
    private int numNodes;
    private int maxChromatic;
    private int numEdges;
    private String fileName;
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
            fileName = file.getName();
            
            this.numNodes = numNodes;
            adjacencySet = new HashSet[numNodes];
            for (int i = 0; i < adjacencySet.length; i++) {
                adjacencySet[i] = new HashSet<Integer>();
            }

            //Checks if e is there or not
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");

                if (parts[0].equalsIgnoreCase("e")) {
                    int edge1 = Integer.parseInt(parts[1]) - 1;
                    int edge2 = Integer.parseInt(parts[2]) - 1;
                    addEdge(edge1, edge2);
                } else if (parts.length == 2) {
                    int edge1 = Integer.parseInt(parts[0]) - 1;
                    int edge2 = Integer.parseInt(parts[1]) - 1;
                    addEdge(edge1, edge2);
                }
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
            throw new RuntimeException("Error reading instance file: " + file.getName());
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

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Instance: ").append(fileName).append("\n");
        sb.append("Number of nodes: ").append(numNodes).append("\n");
        sb.append("Number of edges: ").append(numEdges).append("\n");
        sb.append("Max Chromatic: ").append(maxChromatic).append("\n");

        return sb.toString();
    }
    
}
