package general.HeuristicClasses;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import general.SolutionClasses.SolutionConflictObjective;
import testhelpers.TestGraphs;
import testhelpers.TestHeuristics;

class GCPHeuristicTest {

    private TestHeuristics.StubGCPHeuristic heuristic;

    @BeforeEach
    void setUp() {
        heuristic = new TestHeuristics.StubGCPHeuristic(
                TestHeuristics.options(TestGraphs.path(3)));
    }

    @Test
    void reportLogsValidSolutions() {
        SolutionConflictObjective solution = new SolutionConflictObjective(
                heuristic.getInstance(), new int[] { 1, 2, 1 }, 2);

        assertTrue(heuristic.report(solution));
        assertNotNull(heuristic.getColoring());
        assertArrayEquals(solution.getColoring(), heuristic.getColoring());
    }

    @Test
    void getColoringReturnsNullWhenLogEmpty() {
        assertNull(heuristic.getColoring());
    }

    @Test
    void entryToStringIncludesColoringMetadata() {
        GCPHeuristic.Entry entry = heuristic.new Entry(new int[] { 1, 2, 1 }, 1.5, 2);
        String text = entry.toString();
        assertTrue(text.contains("Colors: 2"));
        assertTrue(text.contains("[1, 2, 1]"));
    }

    @Test
    void reportReturnsFalseWhenRuntimeLimitIsZero() {
        var options = TestHeuristics.options(TestGraphs.path(2));
        options.runtime = 0.0;
        TestHeuristics.StubGCPHeuristic shortRun = new TestHeuristics.StubGCPHeuristic(options);
        assertFalse(shortRun.report());
    }
}
