package general.SolutionClasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import general.Instance;
import general.Move;
import general.HeuristicClasses.Heuristic;
import testhelpers.SeededTestBase;
import testhelpers.TestGraphs;

class SolutionTest extends SeededTestBase {

    private Instance graph;

    @BeforeEach
    void setUpGraph() {
        graph = TestGraphs.path(4);
    }

    @Test
    void randomColoringUsesColorsInRange() {
        int[] coloring = Solution.randomColoring(graph, 3);
        for (int color : coloring) {
            assertTrue(color >= 1 && color <= 3);
        }
    }

    @Test
    void randomColoringIsReproducibleWithSeed() {
        Heuristic.setRandSeed(TEST_SEED);
        int[] first = Solution.randomColoring(graph, 4);
        Heuristic.setRandSeed(TEST_SEED);
        int[] second = Solution.randomColoring(graph, 4);
        assertTrue(Arrays.equals(first, second));
    }

    @Test
    void randTraversalOrderIsPermutation() {
        List<Integer> order = Solution.randTraversalOrder(graph);
        assertEquals(graph.getNumNodes(), order.size());
        for (int i = 0; i < graph.getNumNodes(); i++) {
            assertTrue(order.contains(i));
        }
    }

    @Test
    void randTraversalOrderIsReproducibleWithSeed() {
        Heuristic.setRandSeed(TEST_SEED);
        ArrayList<Integer> first = Solution.randTraversalOrder(graph);
        Heuristic.setRandSeed(TEST_SEED);
        ArrayList<Integer> second = Solution.randTraversalOrder(graph);
        assertEquals(first, second);
    }

    @Test
    void calcKCountsDistinctUsedColors() {
        SolutionConflictObjective solution = new SolutionConflictObjective(
                graph, new int[] { 1, 2, 1, 3 }, 4);
        assertEquals(3, solution.calcK());
        assertEquals(3, solution.getK());
    }

    @Test
    void equalsComparesColoringAndK() {
        SolutionConflictObjective first = new SolutionConflictObjective(
                graph, new int[] { 1, 2, 1, 2 }, 2);
        SolutionConflictObjective second = new SolutionConflictObjective(
                graph, new int[] { 1, 2, 1, 2 }, 2);
        SolutionConflictObjective different = new SolutionConflictObjective(
                graph, new int[] { 1, 2, 2, 2 }, 2);

        assertEquals(first, second);
        assertNotEquals(first, different);
    }

    @Test
    void makeMoveSkipsNoOpMoves() {
        SolutionConflictObjective solution = new SolutionConflictObjective(
                graph, new int[] { 1, 2, 1, 2 }, 2);
        int before = solution.getObjective();
        solution.makeMove(new Move(0, 1, solution));
        assertEquals(before, solution.getObjective());
    }

    @Test
    void validityCheckRejectsOutOfRangeColors() {
        SolutionConflictObjective solution = new SolutionConflictObjective(
                graph, new int[] { 0, 2, 1, 2 }, 2);
        assertThrows(Exception.class, () -> solution.validityCheck(2));
    }

    @Test
    void validityCheckAllowsMinusOneToSkip() throws Exception {
        SolutionConflictObjective solution = new SolutionConflictObjective(
                graph, new int[] { 0, 2, 1, 2 }, 2);
        solution.validityCheck(-1);
    }

    @Test
    void copyConstructorClonesColoring() {
        SolutionConflictObjective original = new SolutionConflictObjective(
                graph, new int[] { 1, 2, 1, 2 }, 2);
        SolutionConflictObjective copy = new SolutionConflictObjective(original);
        copy.getColoring()[0] = 9;
        assertEquals(1, original.getColoring()[0]);
    }

    @Test
    void randMoveChoosesDifferentColorWhenPossible() {
        SolutionConflictObjective solution = new SolutionConflictObjective(
                graph, new int[] { 1, 2, 1, 2 }, 2);
        Move move = solution.randMove();
        assertFalse(move.getColor() == solution.getNodeColor(move.getNode()) && solution.getK() > 1);
    }
}
