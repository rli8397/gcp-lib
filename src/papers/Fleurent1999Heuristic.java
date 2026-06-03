package papers;

import general.*;
import java.util.*;
import java.util.function.ToIntFunction;
import general.SolutionClasses.*;
import general.HeuristicClasses.*;

/*
 * Fleurent & Ferland (1999) style heuristics.
 *
 */
public class Fleurent1999Heuristic extends GCPWrapper {
	private String algorithm;
	private int popSize;
	private int nb_gen;
	private int nb_per_gen;
	private double pm;
	private String crossover;
	private double r;

	public Fleurent1999Heuristic(Options options) {
		super(options);
		this.algorithm = "order_ga";
		this.popSize = parseIntOption("popsize", 50);
		this.nb_gen = parseIntOption("nb_gen", 1000);
		this.nb_per_gen = parseIntOption("nb_per_gen", 3); // number of candidates generated per generation
		this.pm = parseDoubleOption("pm", 0.02); // mutation probability
		this.crossover = parseStringOption("crossover", "one_point");
		this.algorithm = parseStringOption("algorithm", "order_ga");
		this.r = parseDoubleOption("r", 1.0); // constant used for parent selection

		// validate options

		// pm in [0, 1]
		if (pm > 1 || pm < 0) {
			throw new RuntimeException("Invalid mutation probability pm: " + pm + ". Must be in [0, 1].");
		}

		// r in [1, 2]
		if (r < 1 || r > 2) {
			throw new RuntimeException("Invalid value for r: " + r + ". Must be in [1, 2].");
		}
	}

	@Override
	protected KCPHeuristic createKCPHeuristic(GCPHeuristic gcpHeuristic, int k, int[] coloring) {
		return new Fleurent1999KCPHeuristic(gcpHeuristic, k, coloring);
	}

	public class Fleurent1999KCPHeuristic extends KCPHeuristic {

		// can just pass null for coloring since each new K can't build upon the last
		// K's solution
		public Fleurent1999KCPHeuristic(GCPHeuristic gcp, int k, int[] coloring) {
			super(gcp, k);
		}

		@Override
		public void run() {
			switch (algorithm) {
				case "order_ga":
					runGA(crossover, "scramble_sublist_mutation", "perm");
					break;
				case "string_ga":
					String stringMut = parseStringOption("mut", "pm");
					runGA(crossover, stringMut, "string");
					break;
				case "hybrid_tabu":
					String mutParam = parseStringOption("mut", "pm");
					String encoding = parseStringOption("encoding", "string");
					runGA(crossover, mutParam, encoding);
					break;
				default:
					throw new RuntimeException("Unknown algorithm: " + algorithm);
			}

			if (gcp != null) {
				gcp.report(this.solution);
			}
		}

		/* ----------------- GA implementations ----------------- */

		// Each solution is represented as a permutation of nodes
		// A permutation is evaluated by applying a greedy coloring in that order and
		// counting edges between conflicting nodes
		private class PermutationBasedSolution extends SolutionConflictObjective {
			final int[] permutation;

			PermutationBasedSolution(int[] permutation, int[] coloring) {
				super(Fleurent1999KCPHeuristic.this.instance, coloring, Fleurent1999KCPHeuristic.this.k);
				this.permutation = permutation;
			}

			PermutationBasedSolution(int[] permutation, SolutionConflictObjective sol) {
				super(sol);
				this.permutation = permutation;
				this.objective = sol.objective;
			}
		}

		/*
		 * Hybrid GA implementation.
		 *
		 * The paper writes the algorithm options as GA(|P|, E in {P,S}, CR in
		 * {1,2,U,A}, MUT in {LS, TS_n, Pm}).
		 * - |P|: population size.
		 * - E: encoding scheme; P = permutation encoding, S = string/color encoding.
		 * - CR: crossover operator; 1 = one-point, 2 = two-point, U = uniform, A =
		 * graph-adapted recombination.
		 * - MUT: mutation operator; LS = local search, TSn = tabu search with n
		 * iterations, Pm = per-gene random recoloring mutation.
		 * - we are adding an additional mutation operator: scramble_sublist_mutation,
		 * which randomly shuffles a random sublist of the solution permutation or
		 * coloring string.
		 * String based GA is run with E = S, CR = {1, 2, U}, MUT = Pm.
		 * Order based GA is run with E = P, CR = A, MUT = scramble_sublist_mutation.
		 */
		private void runGA(String crossoverType, String mutType, String encoding) {
			int n = this.instance.getNumNodes();
			if ("perm".equalsIgnoreCase(encoding)) {
				ArrayList<PermutationBasedSolution> population = new ArrayList<>(popSize);
				for (int i = 0; i < popSize; i++) {
					int[] perm = randomPermutation(n);
					int[] coloring = greedyColoringFromPermutation(perm, this.k);
					population.add(new PermutationBasedSolution(perm, coloring));
				}
				population.sort(Comparator.comparingInt(PermutationBasedSolution::getObjective));

				for (int gen = 0; gen < nb_gen && report(); gen++) {
					ArrayList<PermutationBasedSolution> children = new ArrayList<>(nb_per_gen);

					for (int i = 0; i < nb_per_gen; i++) {
						int[] parents = selectTwoRankParents(population.size(), r);
						int p1 = parents[0];
						int p2 = parents[1];
						int[] childPerm = permutationCrossover(population.get(p1).permutation,
								population.get(p2).permutation);
						int[] childColor = greedyColoringFromPermutation(childPerm, this.k);

						SolutionConflictObjective mutated = mutate(childColor, mutType);

						PermutationBasedSolution childSol = new PermutationBasedSolution(childPerm, mutated);
						children.add(childSol);

						if (childSol.getObjective() == 0) {
							this.solution = childSol;
							return;
						}
					}

					population = cullPopulation(population, children, PermutationBasedSolution::getObjective); // perm
																												// based
																												// uses
																												// conflict
																												// edges
					double D = computePopulationDiversity(population, this.k);
					r = 1.0 + D;
				}
			} else if ("string".equalsIgnoreCase(encoding)) {
				ArrayList<SolutionConflictCounts> population = new ArrayList<>(popSize);
				for (int i = 0; i < popSize; i++) {
					population.add(new SolutionConflictCounts(this.instance, randomColorString(n, this.k), this.k));
				}
				population.sort(Comparator.comparingInt(SolutionConflictCounts::getConflictedNodeCount));

				for (int gen = 0; gen < nb_gen && report(); gen++) {
					ArrayList<SolutionConflictCounts> children = new ArrayList<>(nb_per_gen);

					for (int i = 0; i < nb_per_gen; i++) {
						int[] parents = selectTwoRankParents(population.size(), r);
						int p1 = parents[0];
						int p2 = parents[1];

						int[][] crossover = crossoverColorings(population.get(p1).getColoring(),
								population.get(p2).getColoring(), crossoverType);
						SolutionConflictCounts childSol1 = mutate(crossover[0], mutType);
						SolutionConflictCounts childSol2 = mutate(crossover[1], mutType);
						children.add(childSol1);
						children.add(childSol2);

						if (childSol1.getConflictedNodeCount() == 0) {
							this.solution = childSol1;
							return;
						}
						if (childSol2.getConflictedNodeCount() == 0) {
							this.solution = childSol2;
							return;
						}
					}

					population = cullPopulation(population, children, SolutionConflictCounts::getConflictedNodeCount); // string
																														// based
																														// uses
																														// conflict
																														// nodes
					double D = computePopulationDiversity(population, this.k);
					r = 1.0 + D;
				}
			} else {
				throw new RuntimeException("Unknown encoding type: " + encoding);
			}
		}

		/* ----------------- GA helpers ----------------- */

		private int[] randomPermutation(int n) {
			int[] p = new int[n];
			for (int i = 0; i < n; i++)
				p[i] = i;
			for (int i = 0; i < n; i++) {
				int j = Heuristic.random(n);
				int tmp = p[i];
				p[i] = p[j];
				p[j] = tmp;
			}
			return p;
		}

		private int[] randomColorString(int n, int k) {
			int[] s = new int[n];
			for (int i = 0; i < n; i++)
				s[i] = Heuristic.random(k) + 1;
			return s;
		}

		/* -----------------Crossover methods --------------------- */

		private int[] permutationCrossover(int[] p1, int[] p2) {
			int n = p1.length;
			int[] child = new int[n];
			boolean[] filled = new boolean[n];
			boolean[] usedGenes = new boolean[n];

			// determines which nodes to copy from p1
			for (int i = 0; i < n; i++) {
				if (Heuristic.random(2) == 1) {
					child[i] = p1[i];
					filled[i] = true;
					usedGenes[p1[i]] = true;
				}
			}

			// Fill the remaining positions from p2 left to right, skipping genes already
			// used.
			int p2Index = 0;
			for (int i = 0; i < n; i++) {
				if (filled[i]) {
					continue;
				}

				while (p2Index < n && usedGenes[p2[p2Index]]) {
					p2Index++;
				}

				if (p2Index >= n) {
					break;
				}

				child[i] = p2[p2Index];
				usedGenes[p2[p2Index]] = true;
				p2Index++;
			}

			return child;
		}

		private int[][] onePointCrossover(int[] s1, int[] s2) {
			int n = s1.length;
			int[][] children = new int[2][n];
			int pt = Heuristic.random(n);

			for (int i = 0; i < pt; i++) {
				children[0][i] = s1[i];
				children[1][i] = s2[i];
			}

			for (int i = pt; i < n; i++) {
				children[0][i] = s2[i];
				children[1][i] = s1[i];
			}
			return children;
		}

		private int[][] twoPointCrossover(int[] s1, int[] s2) {
			int n = s1.length;
			int[][] children = new int[2][n];
			int a = Heuristic.random(n);
			int b = Heuristic.random(n);
			if (a > b) {
				int t = a;
				a = b;
				b = t;
			}

			for (int i = 0; i < a; i++) {
				children[0][i] = s2[i];
				children[1][i] = s1[i];
			}

			for (int i = a; i <= b; i++) {
				children[0][i] = s1[i];
				children[1][i] = s2[i];
			}

			for (int i = b + 1; i < n; i++) {
				children[0][i] = s2[i];
				children[1][i] = s1[i];
			}

			return children;
		}

		private int[][] uniformCrossover(int[] s1, int[] s2) {
			int n = s1.length;
			int[][] children = new int[2][n];
			for (int i = 0; i < n; i++) {
				if (Heuristic.random(2) == 1) {
					children[0][i] = s1[i];
					children[1][i] = s2[i];
				} else {
					children[0][i] = s2[i];
					children[1][i] = s1[i];
				}
			}
			return children;
		}

		private int[] graphAdaptedRecombination(int[] s1, int[] s2) {
			int n = s1.length;
			int[] child = new int[n];
			boolean[] conflicted1 = conflictedNodes(s1);
			boolean[] conflicted2 = conflictedNodes(s2);

			for (int x = 0; x < n; x++) {
				if (conflicted1[x] && !conflicted2[x]) {
					child[x] = s2[x];
				} else if (conflicted2[x] && !conflicted1[x]) {
					child[x] = s1[x];
				} else if (conflicted1[x] && conflicted2[x]) {
					child[x] = leastSupportedNeighborColor(s1, s2, x);
				} else if (Heuristic.random(2) == 1) {
					child[x] = s1[x];
				} else {
					child[x] = s2[x];
				}
			}

			return child;
		}

		private boolean[] conflictedNodes(int[] coloring) {
			boolean[] conflicted = new boolean[coloring.length];
			for (int x = 0; x < coloring.length; x++) {
				for (int neighbor : instance.getAdjacent(x)) {
					if (coloring[x] == coloring[neighbor]) {
						conflicted[x] = true;
						break;
					}
				}
			}
			return conflicted;
		}

		private int leastSupportedNeighborColor(int[] s1, int[] s2, int node) {
			int bestColor = 1;
			int bestSupport = Integer.MAX_VALUE;

			for (int color = 1; color <= this.k; color++) {
				int support = 0;
				for (int neighbor : instance.getAdjacent(node)) {
					if (s1[neighbor] == color || s2[neighbor] == color) {
						support++;
					}
				}

				if (support < bestSupport) {
					bestSupport = support;
					bestColor = color;
				}
			}

			return bestColor;
		}

		private int[][] crossoverColorings(int[] s1, int[] s2, String crossoverType) {
			String type = crossoverType == null ? "one_point" : crossoverType.toLowerCase().trim();
			switch (type) {
				case "two_point":
					return twoPointCrossover(s1, s2);
				case "uniform":
					return uniformCrossover(s1, s2);
				case "graph-adapted":
				case "A":
					return new int[][] {
							graphAdaptedRecombination(s1, s2),
							graphAdaptedRecombination(s1, s2)
					};
				case "one_point":
					return onePointCrossover(s1, s2);
				default:
					throw new RuntimeException("Unknown crossover type: " + crossoverType);
			}
		}

		/* -------------- Mutation methods ---------------------- */

		// scramble a random sublist of an array
		private int[] scrambleSublistMutation(int[] permutation) {
			int mutated[] = permutation.clone();
			int n = permutation.length;
			if (n < 2) {
				return mutated;
			}

			int a = Heuristic.random(n);
			int b = Heuristic.random(n);
			if (a > b) {
				int t = a;
				a = b;
				b = t;
			}

			for (int i = b; i > a; i--) {
				int j = a + Heuristic.random(i - a + 1);
				int tmp = mutated[i];
				mutated[i] = mutated[j];
				mutated[j] = tmp;
			}
			return mutated;
		}

		/* -------------- Hybrid Mutation Methods ---------------------- */
		private SolutionConflictCounts mutate(int[] coloring, String mut) {
			if (mut == null) {
				return new SolutionConflictCounts(this.instance, coloring.clone(), this.k);
			}

			String m = mut.toLowerCase().trim();
			switch (m) {
				case "ls": {
					SolutionConflictCounts solution = new SolutionConflictCounts(this.instance, coloring.clone(),
							this.k);
					int iters = getIntOption("lsiterations", 100);
					return localSearchMutateString(solution, iters);
				}
				case "ts": {
					SolutionConflictCounts solution = new SolutionConflictCounts(this.instance, coloring.clone(),
							this.k);
					int iters = getIntOption("tsiterations", 100);
					return tabuSearchMutateString(solution, iters);
				}
				case "per_gene_shuffle_mutation":
					return new SolutionConflictCounts(this.instance, perGeneShuffleMutation(coloring), this.k);
				case "scramble_sublist_mutation":
					return new SolutionConflictCounts(this.instance, scrambleSublistMutation(coloring), this.k);
				default:
					return new SolutionConflictCounts(this.instance, coloring.clone(), this.k);
			}
		}

		// for each node, with probability pm, assign a new random color
		private int[] perGeneShuffleMutation(int[] coloring) {
			int[] mutated = coloring.clone();
			for (int i = 0; i < mutated.length; i++) {
				if (Heuristic.random() < pm) {
					mutated[i] = Heuristic.random(this.k) + 1;
				}
			}
			return mutated;
		}

		/*
		 * search the neighborhood of the given solution by iteratively picking a random
		 * conflicted node
		 * and recoloring it with a random color, accepting strictly improving moves
		 * Repeat for a given number of iterations or until no conflicted nodes remain.
		 */
		private SolutionConflictCounts localSearchMutateString(SolutionConflictCounts s, int iterations) {
			for (int i = 0; i < iterations && report(); i++) {
				Move m = s.randConflictedMove();
				if (m == null)
					break;
				int currObj = s.getConflictedNodeCount();
				int neighObj = m.getObjective();
				if (neighObj < currObj) {
					s.makeMove(m);
				}
			}
			return s;
		}

		private SolutionConflictCounts tabuSearchMutateString(SolutionConflictCounts s, int iterations) {
			FleurentTabuSearch ts = new FleurentTabuSearch(s, iterations);
			ts.hertzTabuSearch();
			return s;
		}

		private class FleurentTabuSearch extends TabuSearch<Move> {
			private final int iterations;
			private int nextTenureUpdate;

			FleurentTabuSearch(SolutionConflictCounts solution, int iterations) {
				super(solution);
				this.iterations = iterations;
				this.nextTenureUpdate = 0;
			}

			public Move generateBestNeighbor(int iteration) {
				updateTenure(iteration);
				removeExpiredTabu(iteration);

				Move best = null;
				int bestObj = Integer.MAX_VALUE;
				int[] coloring = solution.getColoring();

				for (int node : ((SolutionConflictCounts) solution).getConflictedNodes()) {
					int oldColor = coloring[node];
					for (int color = 1; color <= k; color++) {
						if (color == oldColor) {
							continue;
						}

						Move candidate = new Move(node, color, solution);
						int obj = candidate.getObjective();
						if (!isTabu(candidate, iteration) && obj < bestObj) {
							bestObj = obj;
							best = candidate;
						}
					}
				}

				return best;
			}

			public boolean stopCondition(int iteration) {
				return iteration >= iterations || solution.isValidSolution() || !report();
			}

			public void tabuAppend(Move move, int iteration) {
				Move tabuMove = new Move(move.getNode(), solution.getColoring()[move.getNode()],
						solution);
				tabuMap.put(tabuMove, iteration + tenure);
			}

			public void makeMove(Move move) {
				solution.makeMove(move);
			}

			/*
			 * Dynamic Tabu Tenure
			 * - the based tenure is the square root of the current objective function value
			 * times k/2
			 * - the tenure is then randomly selected in the range [0.9*base, 1.1*base] to
			 * introduce some noise
			 * - the tenure is updated every 2*tMax iterations
			 */
			private void updateTenure(int iteration) {
				if (iteration < nextTenureUpdate) {
					return;
				}

				int f = ((SolutionConflictCounts) solution).getObjective();
				int base = Math.max(1, (int) Math.round(Math.sqrt(f) * k / 2.0));
				int tMin = Math.max(1, (int) Math.floor(0.9 * base));
				int tMax = Math.max(tMin, (int) Math.ceil(1.1 * base));
				this.tenure = tMin + Heuristic.random(tMax - tMin + 1);
				this.nextTenureUpdate = iteration + 2 * tMax;
			}

			private void removeExpiredTabu(int iteration) {
				for (Move move : tabuMap.keySet()) {
					if (tabuMap.get(move) < iteration) {
						tabuMap.remove(move);
					}
				}
			}
		}

		/* -------------- Misc GA helpers ---------------------- */

		/*
		 * Keep the best populationSize solutions from the current parents and newly
		 * generated children using a bounded max-heap.
		 *
		 * Approach: insert every parent and child into a priority queue ordered by
		 * descending objective, evicting the worst item whenever the heap grows past
		 * populationSize. Polling the heap then yields solutions from worst to best,
		 * so we fill the next population from the end to restore ascending rank order.
		 *
		 * Complexity: O((P + C) log P), where P is the population size and C is the
		 * number of children.
		 */
		private <T> ArrayList<T> cullPopulation(ArrayList<T> population, ArrayList<T> children,
				ToIntFunction<T> objectiveFn) {
			PriorityQueue<T> pq = new PriorityQueue<T>(population.size(), new Comparator<T>() {
				@Override
				public int compare(T a, T b) {
					return Integer.compare(objectiveFn.applyAsInt(b), objectiveFn.applyAsInt(a));
				}
			});

			for (T solution : population) {
				pq.offer(solution);
			}

			for (T solution : children) {
				pq.offer(solution);
				if (pq.size() > population.size()) {
					pq.poll();
				}
			}

			ArrayList<T> nextPopulation = new ArrayList<T>(population.size());
			for (int i = 0; i < population.size(); i++) {
				nextPopulation.add(null);
			}

			int idx = population.size() - 1;
			while (!pq.isEmpty()) {
				nextPopulation.set(idx, pq.poll());
				idx--;
			}

			return nextPopulation;
		}

		/*
		 * Selects an index for a parent based on parameter r, see paper for formula
		 */
		private int rankSelectIndex(int N, double r) {
			double u = Heuristic.random() * Math.pow(N, 1 / r); // random number in [0, N^(1/r))
			int idx = (int) Math.ceil(Math.pow(u, r)); // index is ceiling of u^r
			if (idx < 0)
				idx = 0;
			if (idx >= N)
				idx = N - 1;
			return idx;
		}

		/*
		 * Select two distinct indices in [0..n-1] using rank sampling without
		 * replacement.
		 * Ensures parents[0] != parents[1] when n>1.
		 * returns an array of length 2: [p1, p2]
		 */
		private int[] selectTwoRankParents(int n, double r) {
			int[] out = new int[2];
			if (n <= 1) {
				out[0] = 0;
				out[1] = 0;
				return out;
			}
			int p1 = rankSelectIndex(n, r);
			// pick from n-1 by mapping indices >= p1 to +1
			int p2 = rankSelectIndex(n - 1, r);
			if (p2 >= p1)
				p2++;

			out[0] = p1;
			out[1] = p2;
			return out;
		}

		/*
		 * Computes the diversity of population, see paper for equations
		 */
		private double computePopulationDiversity(List<? extends Solution> popSol, int k) {
			int N = popSol.size();
			int n = this.instance.getNumNodes();

			double sumDi = 0.0;
			for (int v = 0; v < n; v++) {
				// index is the color
				// value is the color count across the population for node v
				int[] counts = new int[k + 1];

				// iterate thorugh population
				for (int i = 0; i < N; i++) {
					int col = popSol.get(i).getColoring()[v];
					counts[col]++;
				}

				double Di = 0.0;
				for (int c = 1; c <= k; c++) {
					if (counts[c] == 0)
						continue;
					double x = (double) counts[c] / (double) N;
					Di -= x * (Math.log(x) / Math.log(k));
				}
				sumDi += Di;
			}
			return sumDi / (double) n;
		}

		/*
		 * Assigns the lowest possible non-conflicted color to each node in the order
		 * specified by the given permutation
		 * If there is no unconflicted color, assign the color that is used by the
		 * fewest neighbors
		 * To break ties when multiple colors class are the same size, pick the
		 * "earlier" color class
		 */
		private int[] greedyColoringFromPermutation(int[] permutation, int k) {
			int n = this.instance.getNumNodes();
			int[] coloring = new int[n];

			for (int i = 0; i < n; i++) {
				int v = permutation[i];

				// scans through what colors are used by neighbors
				int[] neighborCounts = new int[k + 1];
				for (int neighbor : instance.getAdjacent(v)) {
					neighborCounts[coloring[neighbor]]++;
				}

				// Finds the color that is neighborCounts with the least number of neighbors
				// To break ties, picks the earlier color class
				int smallestColorClass = 1;
				for (int c = 1; c <= k; c++) {
					if (neighborCounts[c] == 0) {
						smallestColorClass = c;
						break;
					} else if (neighborCounts[c] < neighborCounts[smallestColorClass]) {
						smallestColorClass = c;
					}
				}
				coloring[v] = smallestColorClass;
			}
			return coloring;
		}

	}
}
