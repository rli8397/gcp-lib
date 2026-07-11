package general;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import general.SolutionClasses.SolutionConflictObjective;
import testhelpers.TestGraphs;

class MoveTest {

    private SolutionConflictObjective solution;

    @BeforeEach
    void setUp() {
        solution = new SolutionConflictObjective(TestGraphs.path(3), new int[] { 1, 1, 2 }, 3);
    }

    @Test
    void equalsUsesNodeAndColorOnly() {
        Move first = new Move(1, 2, solution);
        Move second = new Move(1, 2, solution);
        Move different = new Move(1, 1, solution);

        assertEquals(first, second);
        assertNotEquals(first, different);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void getObjectiveLazilyCalculatesFromSolution() {
        Move move = new Move(1, 3, solution);
        assertEquals(0, move.getObjective());
        assertEquals(0, move.getObjective());
    }

    @Test
    void gettersReturnConstructorValues() {
        Move move = new Move(2, 1, solution);
        assertEquals(2, move.getNode());
        assertEquals(1, move.getColor());
    }

    @Test
    void setObjectiveOverridesLazyCalculation() {
        Move move = new Move(1, 2, solution);
        move.setObjective(99);
        assertEquals(99, move.getObjective());
    }
}
