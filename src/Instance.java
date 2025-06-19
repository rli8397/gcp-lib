import java.util.HashSet;
import java.io.File; 
import java.util.Scanner; 

public class Instance {
    private HashSet<Integer>[] adjacencySet; // adjacency matrix is boolean because it is non-directed and unweighted
    
    @SuppressWarnings("unchecked")
    public Instance(int numNodes) {
        adjacencySet = new HashSet[numNodes];
        for (int i = 0; i < numNodes; i++) {
            adjacencySet[i] = new HashSet<Integer>();
        }
    }

    public Instance (File file) {
        try {
            Scanner scanner = new Scanner(file);
            int numNodes = scanner.nextInt();
            adjacencySet = new HashSet[numNodes];
            for (int i = 0; i < numNodes; i++) {
                adjacencySet[i] = new HashSet<Integer>();
            }
            while (scanner.hasNextInt()) {
                int edge1 = scanner.nextInt();
                int edge2 = scanner.nextInt();
                addEdge(edge1, edge2);
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this void method adds an edge between two given nodes
    public void addEdge(int edge1, int edge2) {
        adjacencySet[edge1].add(edge2);
        adjacencySet[edge2].add(edge1); // since the graph is undirected, we add both directions
    }

    // returns an array of size n + 1, the first element in the array is number of neighbors, followed by the neighbors themselves
    public HashSet<Integer> getAdjacent(int node) {
        return adjacencySet[node];
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
