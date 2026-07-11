package general.HeuristicClasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import general.HeuristicClasses.Heuristic;
import general.SolutionClasses.SolutionConflictObjective;
import testhelpers.SeededTestBase;
import testhelpers.TestGraphs;
import testhelpers.TestHeuristics;

class GCPWrapperTest extends SeededTestBase {

    @Test
    void randomRestartProducesSeededColoring() {
        TestHeuristics.StubGCPWrapper wrapper = new TestHeuristics.StubGCPWrapper(
                TestHeuristics.options(TestGraphs.path(4)), null);
        Heuristic.setRandSeed(TEST_SEED);
        int[] first = wrapper.randomRestart(3);
        Heuristic.setRandSeed(TEST_SEED);
        int[] second = wrapper.randomRestart(3);
        assertEquals(first.length, 4);
        assertEquals(first[0], second[0]);
        for (int color : first) {
            assertTrue(color >= 1 && color <= 3);
        }
    }

    @Test
    void reportReducesKForValidSolution() {
        SolutionConflictObjective valid = new SolutionConflictObjective(
                TestGraphs.path(3), new int[] { 1, 2, 1 }, 3);
        TestHeuristics.StubGCPWrapper wrapper = new TestHeuristics.StubGCPWrapper(
                TestHeuristics.options(TestGraphs.path(3)), valid);

        assertTrue(wrapper.report(valid));
        assertNotNull(wrapper.getColoring());
        assertEquals(2, valid.calcK());
    }

    @Test
    void unknownReduceStrategyThrows() {
        var options = TestHeuristics.options(TestGraphs.path(3));
        options.extras.put("reduce_k_strategy", "unknown");
        assertThrows(RuntimeException.class, () -> new TestHeuristics.StubGCPWrapper(options, null));
    }
}
