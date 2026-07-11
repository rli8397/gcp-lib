package testhelpers;

import general.HeuristicClasses.GCPHeuristic;
import general.HeuristicClasses.GCPWrapper;
import general.HeuristicClasses.KCPHeuristic;
import general.Options;
import general.SolutionClasses.Solution;
import general.SolutionClasses.SolutionConflictObjective;

public final class TestHeuristics {
    private TestHeuristics() {
    }

    public static Options options(general.Instance instance) {
        Options options = new Options();
        options.instance = instance;
        options.seed = SeededTestBase.TEST_SEED;
        options.runtime = 3600.0;
        options.verbosity = 0;
        return options;
    }

    public static class StubGCPHeuristic extends GCPHeuristic {
        public StubGCPHeuristic(Options options) {
            super(options);
        }

        @Override
        public void run() {
        }
    }

    public static class StubKCPHeuristic extends KCPHeuristic {
        private final Solution solutionToReport;
        public int runCount;

        public StubKCPHeuristic(GCPHeuristic gcp, int k, Solution solutionToReport) {
            super(gcp, k);
            this.solutionToReport = solutionToReport;
        }

        public StubKCPHeuristic(Options options, int k, Solution solutionToReport) {
            super(options, k);
            this.solutionToReport = solutionToReport;
        }

        @Override
        public void run() {
            runCount++;
            if (gcp != null && solutionToReport != null) {
                gcp.report(solutionToReport);
            }
        }
    }

    public static class StubGCPWrapper extends GCPWrapper {
        private final Solution nextSolution;

        public StubGCPWrapper(Options options, Solution nextSolution) {
            super(options);
            this.nextSolution = nextSolution;
        }

        @Override
        protected KCPHeuristic createKCPHeuristic(GCPHeuristic gcpHeuristic, int k, int[] coloring) {
            if (nextSolution != null) {
                return new StubKCPHeuristic(this, k, nextSolution);
            }

            SolutionConflictObjective solution;
            if (coloring == null) {
                coloring = new int[instance.getNumNodes()];
                for (int i = 0; i < coloring.length; i++) {
                    coloring[i] = (i % Math.max(1, k)) + 1;
                }
            }
            solution = new SolutionConflictObjective(instance, coloring.clone(), k);
            return new StubKCPHeuristic(this, k, solution);
        }
    }
}
