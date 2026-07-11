package papers;

import java.util.HashMap;

import general.HeuristicClasses.Heuristic;
import general.SolutionClasses.Solution;

public abstract class TabuSearch<T> {
    protected int tenure;
    protected HashMap<T, Integer> tabuMap;
    protected Solution solution;
    protected Heuristic heuristic;

    public TabuSearch(Solution solution, Heuristic heuristic) {
        this.solution = solution;
        tabuMap = new HashMap<>();
        this.heuristic = heuristic;
    }

    // checks to see if a move is tabu based on the tabu map and the current
    // iteration
    public boolean isTabu(T move, int iteration) {
        return tabuMap.containsKey(move) && iteration <= tabuMap.get(move);
    }

    public int getTenure() {
        return tenure;
    }

    public abstract T generateBestNeighbor(int iteration);

    public void tabuAppend(T tabuMove, int iteration) {
        // a move is still tabu as long as the iteration is
        // <= curr iteration + tabuTenure
        tabuMap.put(tabuMove, iteration + tenure);
    }

    public boolean stopCondition(int iteration) {
        return !heuristic.report();
    }

    public abstract void tabuSearch();

}