package general;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import testhelpers.TestGraphs;
import testhelpers.TestPaths;

class InstanceTest {

    @Test
    void programmaticGraphTracksEdgesAndDegree() {
        Instance graph = TestGraphs.fromEdges(4, new int[][] {
                { 0, 1 }, { 1, 2 }, { 2, 3 }
        });

        assertEquals(4, graph.getNumNodes());
        assertEquals(3, graph.getNumEdges());
        assertEquals(2, graph.getDegree(1));
        assertTrue(graph.areAdjacent(1, 2));
        assertFalse(graph.areAdjacent(0, 3));
    }

    @Test
    void completeGraphHasCorrectMaxChromatic() {
        Instance graph = TestGraphs.complete(5);
        assertEquals(5, graph.getMaxChromatic());
        assertEquals(10, graph.getNumEdges());
    }

    @Test
    void duplicateEdgeDoesNotIncreaseEdgeCount() {
        Instance graph = TestGraphs.empty(3);
        graph.addEdge(0, 1);
        graph.addEdge(0, 1);
        graph.finalizeMetrics();
        assertEquals(1, graph.getNumEdges());
    }

    @Test
    void loadsPlainEdgeListFile() {
        Instance graph = new Instance(TestPaths.repoFile("testInstances/test.txt"));
        assertEquals(6, graph.getNumNodes());
        assertTrue(graph.areAdjacent(0, 1));
        assertTrue(graph.getMaxChromatic() >= 4);
    }

    @Test
    void toStringIncludesMetadata() {
        Instance graph = TestGraphs.path(3);
        String text = graph.toString();
        assertTrue(text.contains("Number of nodes: 3"));
        assertTrue(text.contains("Number of edges: 2"));
        assertTrue(text.contains("Max Chromatic"));
    }
}
