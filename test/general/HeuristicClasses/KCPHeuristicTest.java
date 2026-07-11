package general.HeuristicClasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import general.Options;
import testhelpers.TestGraphs;
import testhelpers.TestHeuristics;

class KCPHeuristicTest {

    @Test
    void standaloneConstructorStoresK() {
        Options options = TestHeuristics.options(TestGraphs.path(3));
        TestHeuristics.StubKCPHeuristic heuristic = new TestHeuristics.StubKCPHeuristic(options, 4, null);
        assertEquals(4, heuristic.getK());
    }

    @Test
    void wrapperConstructorLinksParentGcpHeuristic() {
        Options options = TestHeuristics.options(TestGraphs.path(3));
        TestHeuristics.StubGCPHeuristic gcp = new TestHeuristics.StubGCPHeuristic(options);
        TestHeuristics.StubKCPHeuristic kcp = new TestHeuristics.StubKCPHeuristic(gcp, 3, null);
        assertNotNull(kcp);
        assertEquals(3, kcp.getK());
    }
}
