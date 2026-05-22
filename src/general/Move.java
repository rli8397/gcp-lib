package general;

import java.util.Objects;

import general.SolutionClasses.Solution;

public class Move {
    protected int node;
    protected int color;
    protected int objective = -1;
    protected Solution solution;

    public Move(int node, int color, Solution solution) {
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
            solution.calcNeighborObjective(this);
        }
        return objective;
    }

    public void setObjective(int objective) {
        this.objective = objective;
    }

    public int getNode() {
        return node;
    }

    public int getColor() {
        return color;
    }

    public String toString() {
        return "Node: " + node + " Color: " + color + " Objective: " + getObjective();
    }
}
