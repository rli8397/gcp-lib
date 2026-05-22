package papers;

import java.util.HashMap;

import general.Move;
import general.HeuristicClasses.Heuristic;
import general.SolutionClasses.SolutionConflictObjective;

public abstract class HertzTabuSearch extends TabuSearch<Move> {
    protected int[] A;

    public HertzTabuSearch(SolutionConflictObjective solution, Heuristic heuristic) {
        super(solution, heuristic);
        this.solution = solution;
        this.A = new int[solution.getInstance().getNumEdges() + 1];
        for (int i = 0; i < A.length; i++) {
            this.A[i] = i - 1;
        }
        tabuMap = new HashMap<>();
    }

    // checks to see if a move is tabu based on the tabu map and the current
    // iteration
    public boolean isTabu(Move move, int iteration) {
        return tabuMap.containsKey(move) && iteration <= tabuMap.get(move);
    }

    public int getTenure() {
        return tenure;
    }

    public abstract Move generateBestNeighbor(int iteration);

    public Move generateBestRepNeighbor(int iteration, int rep) {
        // in this paper, only a "rep" number of neighbors are generated
        // out of those rep neighbors, the best is then returned
        int bestObj = Integer.MAX_VALUE;
        Move bestMove = null;
        int i = 0;
        int loopCount = 1;
        SolutionConflictObjective conflictSolution = (SolutionConflictObjective) solution;

        while (i < rep) {
            // if 1000 iterations have passed without finding a single possible neighbor
            // we are assuming that a neighbor can't be found with
            // the given criteria and are going to return any random move
            // this is a judgement call and is not stated in the paper
            if (loopCount % 1000 == 0 && i == 0) {
                // if no move has been made after 1000 iterations, return any random move
                Move move = solution.randMove();

                return move;
            }
            
            Move currMove = conflictSolution.randConflictedMove();
            int currObj = currMove.getObjective();
            if (!isTabu(currMove, iteration) || currObj <= A[conflictSolution.getObjective()]) {
                if (currObj < bestObj) {
                    bestObj = currObj;
                    bestMove = currMove;
                    if (bestObj <= A[conflictSolution.getObjective()]) {
                        A[conflictSolution.getObjective()] = bestObj - 1;
                    }

                    if (bestObj < conflictSolution.getObjective()) {
                        break;
                    }
                }
                i += 1;
            }
            loopCount++;

        }
        return bestMove;
    }

    public void tabuAppend(Move move, int iteration) {
        Move tabuMove = new Move(move.getNode(), solution.getColoring()[move.getNode()], solution);
        // a move is still tabu as long as the iteration is
        // <= curr iteration + tabuTenure
        tabuMap.put(tabuMove, iteration + getTenure());
    }

    public void tabuSearch() {
        int iteration = 0;

        while (!stopCondition(iteration)) {
            Move neighbor = generateBestNeighbor(iteration);
            if (neighbor == null) {
                break;
            }

            tabuAppend(neighbor, iteration);
            solution.makeMove(neighbor);
            iteration++;
        }
    }

}