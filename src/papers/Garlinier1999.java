package papers;
import general.Heuristic;
import general.Instance;
import papers.Hertz1987.Hertz1987Solution;

public class Garlinier1999 {
    public Garlinier1999(Instance instance, double runtime, int popSize) {
        Heuristic heuristic = new Garlinier1999Heuristic(instance, runtime, popSize);
    }
    public class Garlinier1999Solution extends Solution{
        private PartitionedSolution[] population;
        private Heuristic heuristic;
        public Garlinier1999Solution(Heuristic heuristic, Instance instance, int popSize) {
            super(heuristic, instance.getNumNodes(), instance, true, false);
        }

        public void InitPopulation(int popSize) {

        }

        public void crossOver2RandParents() {

        }

        public Solution crossOver(PartitionedSolution s1, PartitionedSolution s2) {
            for (int l = 1; l <= k; l++) {
                if (l % 2 == 1) {
                    int i = s1.getMaxCardinalityClass();
                    // choose i such that V_i^1 has max cardinality
                } else {
                    int i = s2.getMaxCardinalityClass();
                    // choose i such that V_i^2 has max cardinality
                }

            }
        }


        public void localSearch() {
            // read more about tabu tenure and rep count

        }

        public void updatePopulation() {

        }

    }

    public class Garlinier1999Heuristic extends Heuristic{
        public Garlinier1999Heuristic(Instance instance, double runtime, int popSize) {
            super(instance, runtime);
            Garlinier1999Solution solution = new Garlinier1999Solution(this, instance, popSize);
            while (!report()) {
                solution.InitPopulation(popSize);
                while (solution.objective > 0) {
                    solution.crossOver2RandParents();
                    solution.localSearch();
                    solution.updatePopulation();
                }
                solution.reduceK();
            }
        }
    }

}
