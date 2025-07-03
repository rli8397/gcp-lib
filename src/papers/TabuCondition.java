package papers;
import java.util.*;

public class TabuCondition extends Condition {
    private Queue<Solution> tabuList;
    private double prevObjective;
    public TabuCondition(Queue<Solution> tabuList, double prevObjective) {
        super();
        this.tabuList = tabuList;
        this.prevObjective = prevObjective;
    }
    public boolean isValid(Solution solution) {
        if (solution.getObjective() < prevObjective) {
            prevObjective = solution.getObjective();
            return true;
        }
        for (Solution s : tabuList) {
            if (s.equals(solution)) {
                return false;
            }
        }
        return true;
    }
}
