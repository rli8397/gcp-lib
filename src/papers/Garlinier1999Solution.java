package papers;
import general.Heuristic;
import general.Instance;
public class Garlinier1999Solution extends Solution{
    private Solution[] population;
    public Garlinier1999Solution(Instance graph, int popSize) {
        super(graph.getNumNodes(), graph, true, false);
        InitPopulation(popSize);
    }

    public void InitPopulation(int popSize) {

    }

    public void crossOver(Solution s1, Solution s2) {

    }

    public void updatePopulation() {

    }

    public void Garlinier1999Solution(Heuristic heuristic) {
        while (heuristic.report()) {
            
        }
    }
}
