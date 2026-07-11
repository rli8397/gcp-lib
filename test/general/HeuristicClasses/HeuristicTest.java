package general.HeuristicClasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import general.Options;
import testhelpers.SeededTestBase;
import testhelpers.TestGraphs;
import testhelpers.TestHeuristics;

class HeuristicTest extends SeededTestBase {

    private TestHeuristics.StubGCPHeuristic heuristic;

    @BeforeEach
    void setUp() {
        heuristic = new TestHeuristics.StubGCPHeuristic(
                TestHeuristics.options(TestGraphs.path(3)));
    }

    @Test
    void setRandSeedMakesRandomReproducible() {
        Heuristic.setRandSeed(7L);
        int first = Heuristic.random(100);
        Heuristic.setRandSeed(7L);
        int second = Heuristic.random(100);
        assertEquals(first, second);
    }

    @Test
    void randomNotEqualAvoidsExcludedValue() {
        for (int i = 0; i < 20; i++) {
            int value = Heuristic.randomNotEqual(5, 2);
            assertTrue(value >= 0 && value < 5);
            assertNotEquals(2, value);
        }
    }

    @Test
    void randomNotEqualFallsBackForInvalidExcludedValue() {
        int value = Heuristic.randomNotEqual(4, 10);
        assertTrue(value >= 0 && value < 4);
    }

    @Test
    void reportReturnsTrueWithinRuntimeLimit() {
        assertTrue(heuristic.report());
    }

    @Test
    void getInstanceReturnsConfiguredGraph() {
        assertSame(TestGraphs.path(3).getNumNodes(), heuristic.getInstance().getNumNodes());
    }

    @Test
    void copyConstructorPreservesConfiguration() {
        Options options = TestHeuristics.options(TestGraphs.path(4));
        options.extras.put("key", "value");
        TestHeuristics.StubGCPHeuristic original = new TestHeuristics.StubGCPHeuristic(options);
        Heuristic copied = new Heuristic(original);
        assertEquals(original.getRuntimeLimit(), copied.getRuntimeLimit());
        assertEquals("value", copied.get_cmdline_arg("key"));
    }

    @Test
    void legacyConstructorStoresInstanceAndRuntime() {
        Heuristic legacy = new Heuristic(TestGraphs.empty(2), 15.0);
        assertEquals(2, legacy.getInstance().getNumNodes());
        assertEquals(15.0, legacy.getRuntimeLimit());
        assertTrue(legacy.report());
    }
}
