package papers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import general.HeuristicClasses.GCPHeuristic;
import general.Instance;
import general.Options;
import general.SolutionClasses.PartialColoring;

/**
 * Glover, Parker & Ryan (1995) tabu branch and bound for graph coloring.
 * Phase 1: danger-based sequential coloring with parameter sweep to find U.
 * Phase 2: destructive/constructive tabu backtracking to improve k.
 */
public class Glover1995Heuristic extends GCPHeuristic {
    private static final double C = 1.0;
    private static final double K_U = 0.025;
    private static final double K_A = 0.33;
    private static final double K1 = 1.0;
    private static final double K3 = 0.025;
    private static final double K4 = 0.01;

    private int upBnd = 1;
    private int loBnd = 4;
    private int phase1Iterations = 5;

    public Glover1995Heuristic(Options options) {
        super(options);
        if (options.extras.containsKey("upbnd")) {
            upBnd = Integer.parseInt(options.extras.get("upbnd"));
        }
        if (options.extras.containsKey("lobnd")) {
            loBnd = Integer.parseInt(options.extras.get("lobnd"));
        }
        if (options.extras.containsKey("phase1iterations")) {
            phase1Iterations = Integer.parseInt(options.extras.get("phase1iterations"));
        }
    }

    @Override
    public void run() {
        int lowerBound = cliqueLowerBound(instance);
        DangerParams bestParams = phase1(lowerBound);
        int upperBound = bestParams.colorsUsed;
        int maxColor = upperBound - 1;

        GloverState state = bestParams.state.copy();
        int ccDependentFromClique = countCcDependent(state, lowerBound);

        while (maxColor >= lowerBound && report()) {
            if (maxColor >= upperBound) {
                break;
            }

            boolean improved = phase2(state, maxColor, lowerBound, bestParams, ccDependentFromClique);
            if (!improved) {
                break;
            }

            upperBound = maxColor;
            reportSolution(state, upperBound);
            maxColor = upperBound - 1;
        }
    }

    private void reportSolution(GloverState state, int k) {
        if (!state.isCompleteValidColoring()) {
            return;
        }
        int actualK = state.countColorsUsed();
        PartialColoring solution = new PartialColoring(instance, state.coloring.clone(), actualK);
        if (solution.isValidSolution()) {
            report(solution);
        }
    }

    // --- Phase 1: parameter sweep ---

    private DangerParams phase1(int lowerBound) {
        DangerParams best = null;
        int bestColors = Integer.MAX_VALUE;

        for (int iter = 0; iter < phase1Iterations; iter++) {
            double nodeK = 1.0 + iter;
            double colorK2 = 1.0 + iter;
            DangerParams params = new DangerParams(nodeK, colorK2);

            GloverState state = new GloverState(instance, instance.getNumNodes());
            dangerColor(state, params, false);

            int colorsUsed = state.countColorsUsed();
            params.state = state;
            params.colorsUsed = colorsUsed;

            if (colorsUsed < bestColors) {
                bestColors = colorsUsed;
                best = params;
            }
        }

        if (best != null) {
            reportSolution(best.state, best.colorsUsed);
        }

        if (best == null) {
            return new DangerParams(1.0, 1.0);
        }
        return best;
    }

    // --- Phase 2: tabu branch and bound ---

    private boolean phase2(GloverState state, int maxColor, int lowerBound,
            DangerParams params, int ccDependentFromClique) {
        HashMap<Integer, Integer> tabuPositions = new HashMap<>();
        int iteration = 0;
        int maxBacktrackDepth = lowerBound + ccDependentFromClique;

        while (report()) {
            state.maxColor = maxColor;
            state.recomputeMetrics();

            if (state.isCompleteValidColoring()) {
                return true;
            }

            int dStar = estimateDStar(state, maxColor);
            if (!destructiveBacktrack(state, maxColor, lowerBound, dStar,
                    maxBacktrackDepth, tabuPositions, iteration)) {
                return false;
            }

            dangerColor(state, params, true);

            if (state.isCompleteValidColoring()) {
                return true;
            }

            iteration++;
        }
        return state.isCompleteValidColoring();
    }

    private boolean destructiveBacktrack(GloverState state, int maxColor, int lowerBound,
            int dStar, int maxBacktrackDepth, HashMap<Integer, Integer> tabuPositions,
            int iteration) {
        int overflowPos = state.findFirstOverflowPosition(maxColor);
        if (overflowPos < 0) {
            overflowPos = state.sequence.size() - 1;
        }

        int extraUncolor = upBnd * dStar;
        int targetPos = Math.max(0, overflowPos - extraUncolor);
        state.uncolorFrom(targetPos);

        int n = state.sequence.size();
        if (n == 0) {
            return false;
        }

        int low = Math.max(lowerBound, Math.max(0, n - loBnd * dStar));
        int high = Math.max(lowerBound, Math.min(n - 1, n - upBnd * dStar));

        Integer backtrackPos = findBacktrackPosition(state, low, high, dStar, n,
                lowerBound, tabuPositions, iteration, false);
        if (backtrackPos == null) {
            backtrackPos = findBacktrackPosition(state, low, high, dStar, n,
                    lowerBound, tabuPositions, iteration, true);
        }

        if (backtrackPos == null) {
            if (targetPos > maxBacktrackDepth) {
                state.uncolorFrom(Math.max(0, targetPos - 1));
                return n > 0;
            }
            return false;
        }

        tabuPositions.put(backtrackPos, iteration + dStar);
        state.uncolorFrom(backtrackPos);
        return true;
    }

    private Integer findBacktrackPosition(GloverState state, int low, int high, int dStar,
            int n, int minColors, HashMap<Integer, Integer> tabuPositions,
            int iteration, boolean aspiration) {
        for (int p = high; p >= low; p--) {
            if (!aspiration && isPositionTabu(p, n, dStar, minColors, tabuPositions, iteration)) {
                continue;
            }
            if (aspiration && !isAspirationPositionTabu(p, n, dStar, tabuPositions, iteration)) {
                continue;
            }
            int node = state.sequence.get(p);
            if (state.isEligibleBacktrackNode(node)) {
                return p;
            }
        }
        return null;
    }

    private boolean isPositionTabu(int p, int n, int dStar, int minColors,
            HashMap<Integer, Integer> tabuPositions, int iteration) {
        if (tabuPositions.containsKey(p) && iteration <= tabuPositions.get(p)) {
            return true;
        }
        if (n >= p && p > n - upBnd * dStar) {
            return true;
        }
        return n - loBnd * dStar > p && p >= minColors;
    }

    private boolean isAspirationPositionTabu(int p, int n, int dStar,
            HashMap<Integer, Integer> tabuPositions, int iteration) {
        if (!(n >= p && p > n - upBnd * dStar)) {
            return false;
        }
        return !tabuPositions.containsKey(p) || iteration > tabuPositions.get(p);
    }

    // --- Danger-based sequential coloring ---

    private void dangerColor(GloverState state, DangerParams params, boolean enforceMaxColor) {
        while (report()) {
            state.recomputeMetrics();

            if (state.isCompleteValidColoring()) {
                break;
            }

            Integer node = state.selectDangerNode(params.nodeK);
            if (node == null) {
                break;
            }

            int color = state.selectDangerColor(node, params.colorK2, enforceMaxColor);
            if (color <= 0) {
                break;
            }

            state.assignColor(node, color);
            state.markNewCcDependent();
        }

        state.colorRemainingNodes();
    }

    private int estimateDStar(GloverState state, int maxColor) {
        int alpha = state.largestColorClass();
        if (maxColor <= 0) {
            return 1;
        }
        double bound = alpha * maxColor / ((maxColor + 1.0) * (maxColor + 1.0));
        return Math.max(1, (int) Math.ceil(bound));
    }

    private int countCcDependent(GloverState state, int maxColor) {
        GloverState copy = state.copy();
        copy.maxColor = maxColor;
        copy.recomputeMetrics();
        int count = 0;
        for (int i = 0; i < instance.getNumNodes(); i++) {
            if (copy.isCcDependent(i)) {
                count++;
            }
        }
        return count;
    }

    // --- Clique lower bound ---

    static int cliqueLowerBound(Instance instance) {
        int n = instance.getNumNodes();
        List<Integer> vertices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            vertices.add(i);
        }
        vertices.sort((a, b) -> Integer.compare(instance.getDegree(b), instance.getDegree(a)));

        int roots = Math.min(3, n);
        int best = 1;
        for (int r = 0; r < roots; r++) {
            int root = vertices.get(r);
            best = Math.max(best, maximalCliqueFromRoot(instance, root));
        }
        return best;
    }

    private static int maximalCliqueFromRoot(Instance instance, int root) {
        int best = 1;
        for (int neighbor : instance.getAdjacent(root)) {
            HashSet<Integer> clique = new HashSet<>();
            clique.add(root);
            clique.add(neighbor);

            boolean extended = true;
            while (extended) {
                extended = false;
                for (int v = 0; v < instance.getNumNodes(); v++) {
                    if (clique.contains(v)) {
                        continue;
                    }
                    boolean adjacentToAll = true;
                    for (int member : clique) {
                        if (!instance.areAdjacent(v, member)) {
                            adjacentToAll = false;
                            break;
                        }
                    }
                    if (adjacentToAll) {
                        clique.add(v);
                        extended = true;
                    }
                }
            }
            best = Math.max(best, clique.size());
        }
        return best;
    }

    // --- Inner types ---

    private static class DangerParams {
        final double nodeK;
        final double colorK2;
        GloverState state;
        int colorsUsed;

        DangerParams(double nodeK, double colorK2) {
            this.nodeK = nodeK;
            this.colorK2 = colorK2;
        }
    }

    private class GloverState {
        final Instance instance;
        int maxColor;
        int[] coloring;
        List<Integer> sequence;
        HashSet<Integer> uncolored;
        int[] differentColored;
        int[] uncoloredCount;
        boolean[] ccDependent;

        GloverState(Instance instance, int maxColor) {
            this.instance = instance;
            this.maxColor = maxColor;
            int n = instance.getNumNodes();
            coloring = new int[n];
            sequence = new ArrayList<>();
            uncolored = new HashSet<>();
            differentColored = new int[n];
            uncoloredCount = new int[n];
            ccDependent = new boolean[n];
            for (int i = 0; i < n; i++) {
                uncolored.add(i);
                uncoloredCount[i] = instance.getDegree(i);
            }
        }

        GloverState copy() {
            GloverState other = new GloverState(instance, maxColor);
            System.arraycopy(coloring, 0, other.coloring, 0, coloring.length);
            other.sequence = new ArrayList<>(sequence);
            other.uncolored = new HashSet<>(uncolored);
            System.arraycopy(differentColored, 0, other.differentColored, 0, differentColored.length);
            System.arraycopy(uncoloredCount, 0, other.uncoloredCount, 0, uncoloredCount.length);
            System.arraycopy(ccDependent, 0, other.ccDependent, 0, ccDependent.length);
            return other;
        }

        void recomputeMetrics() {
            int n = instance.getNumNodes();
            uncolored.clear();
            for (int i = 0; i < n; i++) {
                if (coloring[i] == 0) {
                    uncolored.add(i);
                }
            }

            for (int i = 0; i < n; i++) {
                HashSet<Integer> neighborColors = new HashSet<>();
                int uncoloredNeighbors = 0;
                for (int adj : instance.getAdjacent(i)) {
                    if (coloring[adj] == 0) {
                        uncoloredNeighbors++;
                    } else {
                        neighborColors.add(coloring[adj]);
                    }
                }
                differentColored[i] = neighborColors.size();
                uncoloredCount[i] = uncoloredNeighbors;
                ccDependent[i] = isCcDependent(i);
            }
        }

        boolean isCcDependent(int node) {
            return differentColored[node] + uncoloredCount[node] < maxColor;
        }

        void markNewCcDependent() {
            for (int i = 0; i < instance.getNumNodes(); i++) {
                if (coloring[i] != 0 && isCcDependent(i)) {
                    removeFromSequence(i);
                    coloring[i] = 0;
                    uncolored.add(i);
                    ccDependent[i] = true;
                }
            }
        }

        void assignColor(int node, int color) {
            if (coloring[node] == 0) {
                sequence.add(node);
                uncolored.remove(node);
            }
            coloring[node] = color;
            recomputeMetrics();
        }

        void removeFromSequence(int node) {
            sequence.removeIf(v -> v == node);
        }

        void uncolorFrom(int position) {
            while (sequence.size() > position) {
                int node = sequence.remove(sequence.size() - 1);
                coloring[node] = 0;
                uncolored.add(node);
            }
            recomputeMetrics();
        }

        int findFirstOverflowPosition(int targetMaxColor) {
            for (int p = 0; p < sequence.size(); p++) {
                if (coloring[sequence.get(p)] > targetMaxColor) {
                    return p;
                }
            }
            return -1;
        }

        boolean isEligibleBacktrackNode(int node) {
            if (isCcDependent(node)) {
                return false;
            }
            int current = coloring[node];
            if (current <= 0) {
                return false;
            }
            for (int c = 1; c <= maxColor; c++) {
                if (c != current && isColorValid(node, c)) {
                    return true;
                }
            }
            return false;
        }

        boolean isColorValid(int node, int color) {
            for (int adj : instance.getAdjacent(node)) {
                if (coloring[adj] == color) {
                    return false;
                }
            }
            return true;
        }

        int firstValidColor(int node) {
            for (int c = 1; c <= maxColor; c++) {
                if (isColorValid(node, c)) {
                    return c;
                }
            }
            if (!enforceMaxColorExtra()) {
                return maxColor + 1;
            }
            return 0;
        }

        private boolean enforceMaxColorExtra() {
            return maxColor < instance.getNumNodes();
        }

        Integer selectDangerNode(double nodeK) {
            List<Integer> candidates = new ArrayList<>();
            for (int node : uncolored) {
                if (!ccDependent[node] && coloring[node] == 0) {
                    candidates.add(node);
                }
            }
            if (candidates.isEmpty()) {
                return null;
            }

            candidates.sort(Comparator.comparingInt((Integer n) -> differentColored[n]).reversed());
            int topSat = differentColored[candidates.get(0)];
            int threshold = topSat;
            int seen = 0;
            for (int node : candidates) {
                if (differentColored[node] < topSat) {
                    seen++;
                    if (seen >= 2) {
                        threshold = differentColored[node];
                        break;
                    }
                }
            }

            Integer bestNode = null;
            double bestDanger = Double.NEGATIVE_INFINITY;
            for (int node : candidates) {
                if (differentColored[node] < threshold) {
                    continue;
                }
                double danger = nodeDanger(node, nodeK);
                if (bestNode == null || danger > bestDanger) {
                    bestDanger = danger;
                    bestNode = node;
                }
            }
            return bestNode;
        }

        double nodeDanger(int node, double nodeK) {
            int sat = differentColored[node];
            int avail = maxColor - sat;
            if (avail <= 0) {
                avail = 1;
            }
            double share = computeShare(node);
            double f = C / Math.pow(Math.max(1, maxColor - sat), nodeK);
            return f + K_U * uncoloredCount[node] + K_A * (share / avail);
        }

        double computeShare(int node) {
            HashSet<Integer> myColors = availableColors(node);
            if (myColors.isEmpty()) {
                return 0;
            }
            double share = 0;
            for (int color : myColors) {
                for (int adj : instance.getAdjacent(node)) {
                    if (uncolored.contains(adj) && isColorValid(adj, color)) {
                        share++;
                        break;
                    }
                }
            }
            return share;
        }

        HashSet<Integer> availableColors(int node) {
            HashSet<Integer> colors = new HashSet<>();
            int limit = Math.max(maxColor, countColorsUsed() + 1);
            for (int c = 1; c <= limit; c++) {
                if (isColorValid(node, c)) {
                    colors.add(c);
                }
            }
            return colors;
        }

        int selectDangerColor(int node, double colorK2, boolean enforceMaxColor) {
            int bestColor = 0;
            double bestDanger = Double.NEGATIVE_INFINITY;
            int colorsUsed = countColorsUsed();
            int colorLimit = enforceMaxColor ? maxColor : Math.max(maxColor, colorsUsed + 1);

            for (int c = 1; c <= colorLimit; c++) {
                if (!isColorValid(node, c)) {
                    continue;
                }
                double danger = colorDanger(c, colorK2);
                if (bestColor == 0 || danger > bestDanger) {
                    bestDanger = danger;
                    bestColor = c;
                }
            }

            if (bestColor == 0 && !enforceMaxColor) {
                bestColor = colorsUsed + 1;
                while (!isColorValid(node, bestColor)) {
                    bestColor++;
                    if (bestColor > instance.getNumNodes()) {
                        return 0;
                    }
                }
            }
            return bestColor;
        }

        double colorDanger(int color, double colorK2) {
            int diffNeighbors = 0;
            int nC = -1;
            int uncoloredNc = 0;

            for (int node : uncolored) {
                if (!isColorValid(node, color)) {
                    continue;
                }
                if (differentColored[node] > diffNeighbors) {
                    diffNeighbors = differentColored[node];
                    nC = node;
                    uncoloredNc = uncoloredCount[node];
                }
            }

            if (nC < 0) {
                return 0;
            }

            int numC = 0;
            for (int i = 0; i < instance.getNumNodes(); i++) {
                if (coloring[i] == color) {
                    numC++;
                }
            }

            double denom = Math.max(1, maxColor - diffNeighbors);
            return K1 / Math.pow(denom, colorK2) + K3 * uncoloredNc - K4 * numC;
        }

        int countColorsUsed() {
            boolean[] used = new boolean[instance.getNumNodes() + 1];
            int count = 0;
            for (int c : coloring) {
                if (c > 0 && !used[c]) {
                    used[c] = true;
                    count++;
                }
            }
            return count;
        }

        int largestColorClass() {
            int[] counts = new int[instance.getNumNodes() + 1];
            int best = 0;
            for (int c : coloring) {
                if (c > 0) {
                    counts[c]++;
                    best = Math.max(best, counts[c]);
                }
            }
            return Math.max(1, best);
        }

        boolean allNodesProcessed() {
            for (int c : coloring) {
                if (c == 0) {
                    return false;
                }
            }
            return true;
        }

        boolean isCompleteValidColoring() {
            if (!allNodesProcessed()) {
                return false;
            }
            for (int i = 0; i < instance.getNumNodes(); i++) {
                if (coloring[i] <= 0) {
                    return false;
                }
                for (int adj : instance.getAdjacent(i)) {
                    if (i < adj && coloring[i] == coloring[adj]) {
                        return false;
                    }
                }
            }
            return countColorsUsed() <= maxColor;
        }

        void colorRemainingNodes() {
            while (report()) {
                recomputeMetrics();
                if (allNodesProcessed()) {
                    break;
                }
                Integer node = null;
                for (int i = 0; i < instance.getNumNodes(); i++) {
                    if (coloring[i] == 0) {
                        node = i;
                        break;
                    }
                }
                if (node == null) {
                    break;
                }
                int color = firstValidColor(node);
                if (color <= 0) {
                    break;
                }
                assignColor(node, color);
            }
        }
    }
}
