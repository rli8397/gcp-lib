package testhelpers;

import general.Instance;

public final class TestGraphs {
    private TestGraphs() {
    }

    public static Instance empty(int nodes) {
        return new Instance(nodes, "empty");
    }

    public static Instance complete(int nodes) {
        Instance graph = new Instance(nodes, "K" + nodes);
        for (int i = 0; i < nodes; i++) {
            for (int j = i + 1; j < nodes; j++) {
                graph.addEdge(i, j);
            }
        }
        graph.finalizeMetrics();
        return graph;
    }

    public static Instance path(int nodes) {
        Instance graph = new Instance(nodes, "path" + nodes);
        for (int i = 0; i < nodes - 1; i++) {
            graph.addEdge(i, i + 1);
        }
        graph.finalizeMetrics();
        return graph;
    }

    public static Instance fromEdges(int nodes, int[][] edges) {
        Instance graph = new Instance(nodes, "custom");
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }
        graph.finalizeMetrics();
        return graph;
    }
}
