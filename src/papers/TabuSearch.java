package papers;

import java.util.HashMap;

import general.HeuristicClasses.GCPHeuristic;
import general.SolutionClasses.Solution;

public abstract class TabuSearch<T> {
    protected int tenure;
    protected HashMap<T, Integer> tabuMap;
    protected int[] A;
    protected Solution solution;
    protected GCPHeuristic heuristic;

    // public TabuSearch(Solution solution) {
    //     this.solution = solution;
    //     this.A = new int[solution.instance.getNumEdges() + 1];
    //     for (int i = 0; i < A.length; i++) {
    //         this.A[i] = i - 1;
    //     }
    //     tabuMap = new HashMap<>();

    // }

    public void initTabu(Solution solution) {
        this.solution = solution;
        this.A = new int[solution.getInstance().getNumEdges() + 1];
        for (int i = 0; i < A.length; i++) {
            this.A[i] = i - 1;
        }
        tabuMap = new HashMap<>();
    }

    // checks to see if a move is tabu based on the tabu map and the current
    // iteration
    public boolean isTabu(T move, int iteration) {
        return tabuMap.containsKey(move) && iteration <= tabuMap.get(move);
    }

    public int getTenure() {
        return tenure;
    }

    // not sure what to do with this
    public abstract T generateBestNeighbor(int iteration);

    public abstract void tabuAppend(T move, int iteration);

    public boolean stopCondition(int iteration) {
        return !heuristic.report();
    }

    public void tabuSearch() {
        int iteration = 0;

        while (!stopCondition(iteration)) {
            T neighbor = generateBestNeighbor(iteration);
            if (neighbor == null) {
                break;
            }

            tabuAppend(neighbor, iteration);

            iteration++;
        }
    }
}
