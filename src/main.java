import java.io.File;

public class main{
    public static void main(String[] args){
        File test= new File ("C:\\Users\\shrey\\OneDrive\\Documents\\GraphCol\\gcp-lib\\test.txt");
        Instance testergraph  = new Instance (test);

        SolutionConflict testing = new SolutionConflict(1,5,testergraph,true, false);

        System.out.println(testing);

    }
}