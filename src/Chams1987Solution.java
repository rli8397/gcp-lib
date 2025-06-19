

public class Chams1987Solution extends Solution{
    public Chams1987Solution(int colors, int n, Instance g) {
        super(colors, n, g, false, false);
    }
    
    public void Chams1987() {
        double t = Math.sqrt(this.graph.getNumNodes());
        double a = 0.93;
        boolean change = true;
        while (change) {
            change = false;
            int rep = (int) Math.exp(2/t);
            for(int i = 0; i < rep; i++) {
                Solution neighbor = this.generateNewNeighbor();
                double delta = neighbor.getObjective() - this.getObjective();
                if (delta < 0) {
                    this.coloring = neighbor.coloring;
                    this.objective += delta;
                    change = true;
                    break;
                    //Keep in mind k is updating and stuff 
                } else {
                    double p = Math.exp(-delta / t);
                    double x = Math.random();
                    if (x < p) {
                        this.coloring = neighbor.coloring;
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
    }
}