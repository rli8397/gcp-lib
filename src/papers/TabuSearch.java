package papers;

import java.util.HashMap;

public abstract class TabuSearch {
    protected int tenure;
    protected HashMap<Move, Integer> tabuMap;
    protected SolutionConflictObjective solution;
    protected int[] A;

    // checks to see if a move is tabu based on the tabu map and the current
    // iteration
    public boolean isTabu(Move move, int iteration) {
        return tabuMap.containsKey(move) && iteration <= tabuMap.get(move);
    }

    public int getTenure() {
        return tenure;
    }

    public Move generateBestRepNeighbor(int rep, int iteration) {
        int bestObj = Integer.MAX_VALUE;
        Move bestMove = null;
        int i = 0;
        int loopCount = 1;

        while (i < rep) {
            // if 1000 iterations have passed, we are assuming that a neighbor can't with
            // the given criteria and are going
            // to return, this is a judgement call and is not stated in the paper
            if (loopCount % 1000 == 0) {
                // if no move has been made, return any random move, other return the best move
                // found so far
                if (i == 0) {
                    return solution.randMove();
                } else {
                    return bestMove;
                }
            }
            
            Move currMove = solution.randConflictedMove();
            int currObj = currMove.getObjective();
            if (!isTabu(currMove, iteration) || currObj <= A[solution.objective]) {
                if (currObj < bestObj) {
                    bestObj = currObj;
                    bestMove = currMove;
                    if (bestObj < solution.objective) {
                        A[solution.objective] = bestObj - 1;
                        break;
                    }
                }
                i += 1;
            }
            loopCount++;
            
        }

        return bestMove;
    }
}
