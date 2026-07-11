package general.SolutionClasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import general.Move;
import testhelpers.SeededTestBase;
import testhelpers.TestGraphs;

class SolutionConflictCountsTest extends SeededTestBase {

    private SolutionConflictCounts solution;

    @BeforeEach
    void setUp() {
        solution = new SolutionConflictCounts(TestGraphs.path(3), new int[] { 1, 1, 2 }, 3);
    }

    @Test
    void initBuildsConflictCountMap() {
        assertEquals(1, solution.getObjective());
        assertTrue(solution.toString().contains("1"));
    }

    @Test
    void doMakeMoveUpdatesConflictCounts() {
        Move move = new Move(1, 3, solution);
        solution.makeMove(move);
        assertEquals(0, solution.getObjective());
        assertEquals(3, solution.getColoring()[1]);
    }

    @Test
    void randConflictedNodeUsesConflictMap() {
        assertEquals(1, solution.randConflictedNode());
    }

    @Test
    void randConflictedNodeReturnsMinusOneWhenNoConflicts() {
        SolutionConflictCounts valid = new SolutionConflictCounts(
                TestGraphs.path(3), new int[] { 1, 2, 1 }, 2);
        assertEquals(-1, valid.randConflictedNode());
    }

    @Test
    void validityCheckPassesForConsistentState() throws Exception {
        solution.validityCheck(2);
    }

    @Test
    void validityCheckFailsWhenConflictMapDrifts() {
        solution.makeMove(new Move(1, 3, solution));
        solution.getColoring()[0] = 3;
        assertThrows(Exception.class, () -> solution.validityCheck(3));
    }

    @Test
    void copyConstructorClonesConflictCounts() {
        SolutionConflictCounts copy = new SolutionConflictCounts(solution);
        assertEquals(solution.getObjective(), copy.getObjective());
        assertFalse(copy.toString().isEmpty());
    }
}
