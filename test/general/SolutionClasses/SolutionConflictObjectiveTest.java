package general.SolutionClasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import general.Move;
import testhelpers.SeededTestBase;
import testhelpers.TestGraphs;

class SolutionConflictObjectiveTest extends SeededTestBase {

    private SolutionConflictObjective solution;

    @BeforeEach
    void setUp() {
        solution = new SolutionConflictObjective(TestGraphs.path(3), new int[] { 1, 1, 2 }, 3);
    }

    @Test
    void initCountsConflictingEdgesOnce() {
        assertEquals(1, solution.getObjective());
        assertFalse(solution.isValidSolution());
    }

    @Test
    void validColoringHasZeroObjective() {
        SolutionConflictObjective valid = new SolutionConflictObjective(
                TestGraphs.path(3), new int[] { 1, 2, 1 }, 2);
        assertEquals(0, valid.getObjective());
        assertTrue(valid.isValidSolution());
    }

    @Test
    void calcNeighborObjectiveTracksConflictDelta() {
        Move move = new Move(1, 3, solution);
        assertEquals(0, move.getObjective());
    }

    @Test
    void makeMoveUpdatesObjectiveAndColoring() {
        Move move = new Move(1, 3, solution);
        solution.makeMove(move);
        assertEquals(3, solution.getColoring()[1]);
        assertEquals(0, solution.getObjective());
    }

    @Test
    void randConflictedNodeReturnsConflictedVertex() {
        int node = solution.randConflictedNode();
        assertEquals(1, node);
    }

    @Test
    void randConflictedNodeReturnsMinusOneWhenValid() {
        SolutionConflictObjective valid = new SolutionConflictObjective(
                TestGraphs.path(3), new int[] { 1, 2, 1 }, 2);
        assertEquals(-1, valid.randConflictedNode());
    }

    @Test
    void randConflictedMoveReturnsMoveForConflictedNode() {
        Move move = solution.randConflictedMove();
        assertNotNull(move);
        assertEquals(1, move.getNode());
        assertFalse(move.getColor() == solution.getColoring()[1]);
    }

    @Test
    void randConflictedMoveReturnsNullWhenValid() {
        SolutionConflictObjective valid = new SolutionConflictObjective(
                TestGraphs.path(3), new int[] { 1, 2, 1 }, 2);
        assertNull(valid.randConflictedMove());
    }

    @Test
    void validityCheckDetectsObjectiveMismatch() {
        solution.objective = 99;
        assertThrows(Exception.class, () -> solution.validityCheck(3));
    }
}
