package papers;

import java.util.Objects;

public class Move {
    protected int node;
    protected int color;
    protected int objective = -1;
    protected SolutionConflictObjective solution;

    public Move(int node, int color, SolutionConflictObjective solution) {
        this.node = node;
        this.color = color;
        this.solution = solution;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Move))
            return false;
        Move other = (Move) obj;
        return node == other.node && color == other.color;
    }

    public int hashCode() {
        return Objects.hash(node, color);
    }

    public int getObjective() {
        if (objective == -1) {
            objective = solution.calcNeighborObjective(this);
        }
        return objective;
    }

    public String toString() {
        return "Node: " + node + " Color: " + color + " Objective: " + getObjective();
    }
}
