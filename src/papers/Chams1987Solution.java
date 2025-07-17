package papers;
import general.Heuristic;
import general.Instance;

public class Chams1987Solution extends Solution{
    public Chams1987Solution(int colors, Instance g) {
        super(colors, g, false, false);
    }
    
    public void Chams1987(Heuristic heuristic) {
        while (heuristic.report()) {
            double t = Math.sqrt(this.graph.getNumNodes());
            double a = 0.93;
            boolean change = true;
            while (change) {
                change = false;
                int rep = (int) Math.exp(2/t);
                for(int i = 0; i < rep; i++) {
                    Move move = generateRandomMove();
                    double delta = calcNeighborObjective(move) - this.getObjective();
                    if (delta < 0) {
                        coloring[move.node] = move.color;
                        this.objective += delta;
                        change = true;
                        break;
                        //Keep in mind k is updating and stuff 
                    } else {
                        double p = Math.exp(-delta / t);
                        double x = Math.random();
                        if (x < p) {
                            coloring[move.node] = move.color;
                            this.objective += delta;
                            if (delta != 0){
                                change = true;
                            }
                            break;
                        }
                    }
                }
                t *= a; // Decreases temperature
            }
            printStatus();
            if (objective == 0) {
                reduceK();
            }
        }
        
    }
}