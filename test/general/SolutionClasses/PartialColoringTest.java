package general.SolutionClasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import general.Move;
import testhelpers.SeededTestBase;
import testhelpers.TestGraphs;
import testhelpers.TestHeuristics;

class PartialColoringTest extends SeededTestBase {

    private PartialColoring solution;

    @BeforeEach
    void setUp() {
        solution = new PartialColoring(TestGraphs.path(4), new int[] { 1, 0, 0, 2 }, 3);
    }

    @Test
    void initLeavesUncoloredNodesInvalid() {
        assertFalse(solution.isValidSolution());
        Move coloringMove = new Move(1, 2, solution);
        assertEquals(1, coloringMove.getObjective());
    }

    @Test
    void calcNeighborObjectiveDecreasesWhenColoringUncoloredNode() {
        Move move = new Move(1, 2, solution);
        assertEquals(1, move.getObjective());
    }

    @Test
    void calcNeighborObjectiveIncreasesWhenUncoloringNode() {
        Move move = new Move(0, 0, solution);
        assertEquals(3, move.getObjective());
    }

    @Test
    void doMakeMoveUncolorsConflictingNeighbors() {
        solution.doMakeMove(new Move(1, 1, solution));
        assertEquals(0, solution.getColoring()[0]);
        assertEquals(1, solution.getColoring()[1]);
    }

    @Test
    void isValidSolutionWhenAllNodesColored() {
        PartialColoring complete = new PartialColoring(
                TestGraphs.path(3), new int[] { 1, 2, 1 }, 2);
        assertTrue(complete.isValidSolution());
    }

    @Test
    void partialColoringProducesConflictFreeColoredNodes() {
        TestHeuristics.StubGCPHeuristic heuristic = new TestHeuristics.StubGCPHeuristic(
                TestHeuristics.options(TestGraphs.path(5)));
        int[] coloring = PartialColoring.partialColoring(heuristic, 3);

        for (int node = 0; node < 5; node++) {
            if (coloring[node] == 0) {
                continue;
            }
            for (int neighbor : heuristic.getInstance().getAdjacent(node)) {
                if (coloring[neighbor] != 0) {
                    assertFalse(coloring[node] == coloring[neighbor]);
                }
            }
        }
    }
}
