# Tabu Branch and Bound - Glover 1995

## Recursive shrinking 
    - Remove dependent nodes
    - The rest of the graph nodes’ degree decreases by 1, so one or more nodes might become dependent, continue remove nodes recursively
    - Keep track of the order in which nodes are removed

## Dependency
    - Node n is dependent if degree(n) < max_color
    - There will always be a color available for dependent nodes that doesn't conflict with neighbors

## Color Conditional dependency (cc-dependent)
    - Potential difference < max_color
    - Potential difference of node n = # of different colors assigned to neighbors (saturation degree) + # uncolored neighbors of n
    - Only applies to partially colored graphs
    - Theorem: can use cc-dependency for recursive node removal for partially coloring graph without changing the chromatic number
    - Idea: 
    - Initially, uncolored(i) is set to the number of neighbors for node i
    - As node j is colored, different colored(i) and uncolored(i) are updated and potential different is recalculated
    - If after updates a node is cc-dependent, set the node color to 0
    - While backtracking, if a node has color 0, then skip over it
    - After completing the recursive shrinking process, coloring the cc-dependent nodes with a valid color

## Node Danger - how dangerous is it if we do not color node i?
    - Dangerous if high different colored(i) value and high uncolored(i)
    - avail(i) = number of colors available to node i = max_color - different colored(i)
    - share(i) = number of colors available to node i that are also available to uncolored neighbors
    - danger(i) = F(different_colored(i)) + k_u * uncolored(i) + k_a * (share(i) / avail(i))
        - F is a monotonic increasing (always increasing) function
            - Paper suggestion: F(y) = C / (max_color - y)^k where C is an arbitrary constant
            - As max_color - y approaches 1 (aka saturation degree increases), the criticality of F becomes more pronounced
        - k_u and k_a are nonnegative constants
    - The node with the highest danger = next node to be colored
        - Since danger is tied to different_colored(i) paper suggests just checking the 2 or 3 highest different_colored values for their danger function instead of parsing the whole uncolored graph
        - Suggested parameters: C = 1.0, k = 1.0, k_u = 0.025, k_a = 0.33

## Color Danger - how dangerous it is to assign a color to a specific node
    - Formula: k_1 / (max_color - diff_neighbors(c))^k_2 + k_3 * uncolored(n_c) - k_4 * num(c)
        - All k values are nonnegative constants
        - C is a color
        - Diff_neighbors(c) = Among all the uncolored nodes that can use c without conflict, take the largest count of distinct colors from neighbors
        - N_c = node achieving the largest count of distinct colors from neighbors (that can be assigned color c)

## Improving K
    - Once a valid coloring with k has been found, improve upon it by setting lower_bound <= k_new < k
    - Then uncolor a set of previously colored nodes to recolor

## Width and Depth of Local Optima
    - GL represents set of global minima, L is set of local minima that are not global minima
    - X is the set of states and v(x) is a real valued function
    - A flat topography (the width is large in relation the depth) might make it hard for an objective driven search
    - Independent set is a set of nonadjacent nodes
        - alpha(G) is the largest independent set of graph G
    - A matching is a set of edges M so that no two edges in M share an endpoint
## Depth 
    - Depth is the minimum distance uphill that must be traveled in order to escape from the local minimum x
    - Let d* = the max depth out of all local min x in L
## Width
    - Given x is in L, y is in X, and v(y) < v(x) – meaning y is a “better” state that x –, the width, w, represents the max amount of nonimproving moves needed in order to get from x to y
    - Let w* = the max width among all local min x in L
    - The width gives an estimate of the necessary length of a tabu list in a tabu search

## Application to Tabu Branch and Bound 
    - When a valid coloring is found, improve by keeping a fixed number of colors na deteriorating between improving and nonimproving moves until either a feasible coloring is found or it is determined that it is not “readily” possible to find a coloring
    - Trial solution will have some but not necessarily all nodes colored
    - A move is uncoloring a colored node or coloring an uncolored node
    - Objective value for a solution is the number of uncolored nodes

## 


