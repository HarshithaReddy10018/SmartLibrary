package com.smartlibrary.algorithms;

import java.util.*;

/**
 * Module 5 (CO5): NP-Completeness and Vertex-Cover 2-Approximation.
 * Applied in SmartLibrary for document-conflict modeling (e.g. conflicting citations, simultaneous review locks, plagiarism disputes).
 * Demonstrates:
 * 1. 2-Approximation using Maximal Matching (guaranteed factor <= 2).
 * 2. Greedy Degree Heuristic.
 * 3. Exact Exponential Solver for small instances to compute the empirical approximation ratio.
 * 4. Karp Reductions: 3-SAT -> CLIQUE -> INDEPENDENT SET -> VERTEX COVER.
 */
public class VertexCoverApproximation {

    public static class Edge {
        public int u;
        public int v;
        public String uName;
        public String vName;

        public Edge(int u, int v, String uName, String vName) {
            this.u = u;
            this.v = v;
            this.uName = uName;
            this.vName = vName;
        }
    }

    public static class CoverResult {
        public String algorithm;
        public List<String> coverVertices;
        public int coverSize;
        public double approximationRatio;
        public long executionTimeNanos;

        public CoverResult(String algorithm, List<String> coverVertices, int coverSize, double approximationRatio, long executionTimeNanos) {
            this.algorithm = algorithm;
            this.coverVertices = coverVertices;
            this.coverSize = coverSize;
            this.approximationRatio = approximationRatio;
            this.executionTimeNanos = executionTimeNanos;
        }
    }

    public static class ComparisonResult {
        public CoverResult approx2;
        public CoverResult greedy;
        public CoverResult exact;
        public int totalVertices;
        public int totalEdges;
        public String reductionChainExplanation;

        public ComparisonResult(CoverResult approx2, CoverResult greedy, CoverResult exact, int totalVertices, int totalEdges, String reductionChainExplanation) {
            this.approx2 = approx2;
            this.greedy = greedy;
            this.exact = exact;
            this.totalVertices = totalVertices;
            this.totalEdges = totalEdges;
            this.reductionChainExplanation = reductionChainExplanation;
        }
    }

    private final int numVertices;
    private final List<String> vertexNames;
    private final List<Edge> edges;

    public VertexCoverApproximation(int numVertices, List<String> vertexNames) {
        this.numVertices = numVertices;
        this.vertexNames = vertexNames;
        this.edges = new ArrayList<>();
    }

    public void addConflictEdge(int u, int v) {
        edges.add(new Edge(u, v, vertexNames.get(u), vertexNames.get(v)));
    }

    /**
     * Standard 2-Approximation algorithm for Vertex Cover using maximal matching.
     */
    public CoverResult solve2Approximation() {
        long startTime = System.nanoTime();
        boolean[] inCover = new boolean[numVertices];
        boolean[] edgeCovered = new boolean[edges.size()];

        for (int i = 0; i < edges.size(); i++) {
            if (!edgeCovered[i]) {
                Edge e = edges.get(i);
                if (!inCover[e.u] && !inCover[e.v]) {
                    inCover[e.u] = true;
                    inCover[e.v] = true;

                    // Mark all incident edges as covered
                    for (int j = 0; j < edges.size(); j++) {
                        Edge other = edges.get(j);
                        if (other.u == e.u || other.v == e.u || other.u == e.v || other.v == e.v) {
                            edgeCovered[j] = true;
                        }
                    }
                }
            }
        }

        List<String> cover = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            if (inCover[i]) cover.add(vertexNames.get(i));
        }

        long elapsed = System.nanoTime() - startTime;
        return new CoverResult("2-Approximation (Maximal Matching)", cover, cover.size(), 0.0, elapsed);
    }

    /**
     * Greedy heuristic picking the vertex with the maximum degree.
     */
    public CoverResult solveGreedy() {
        long startTime = System.nanoTime();
        boolean[] edgeCovered = new boolean[edges.size()];
        int uncoveredCount = edges.size();
        List<String> cover = new ArrayList<>();
        boolean[] inCover = new boolean[numVertices];

        while (uncoveredCount > 0) {
            int[] degree = new int[numVertices];
            for (int i = 0; i < edges.size(); i++) {
                if (!edgeCovered[i]) {
                    degree[edges.get(i).u]++;
                    degree[edges.get(i).v]++;
                }
            }

            int bestV = -1;
            int maxDeg = -1;
            for (int i = 0; i < numVertices; i++) {
                if (!inCover[i] && degree[i] > maxDeg) {
                    maxDeg = degree[i];
                    bestV = i;
                }
            }

            if (bestV == -1 || maxDeg == 0) break;

            inCover[bestV] = true;
            cover.add(vertexNames.get(bestV));

            for (int i = 0; i < edges.size(); i++) {
                if (!edgeCovered[i]) {
                    if (edges.get(i).u == bestV || edges.get(i).v == bestV) {
                        edgeCovered[i] = true;
                        uncoveredCount--;
                    }
                }
            }
        }

        long elapsed = System.nanoTime() - startTime;
        return new CoverResult("Greedy Degree Heuristic", cover, cover.size(), 0.0, elapsed);
    }

    /**
     * Exact exponential solver (branch and bound or power set) for small N (<= 25).
     */
    public CoverResult solveExact() {
        long startTime = System.nanoTime();
        if (numVertices > 25) {
            return new CoverResult("Exact Optimal Solver", Collections.emptyList(), -1, 1.0, 0);
        }

        int bestMask = (1 << numVertices) - 1;
        int minSize = numVertices;

        for (int mask = 0; mask < (1 << numVertices); mask++) {
            int size = Integer.bitCount(mask);
            if (size >= minSize) continue;

            boolean valid = true;
            for (Edge e : edges) {
                if (((mask & (1 << e.u)) == 0) && ((mask & (1 << e.v)) == 0)) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                minSize = size;
                bestMask = mask;
            }
        }

        List<String> cover = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            if ((bestMask & (1 << i)) != 0) {
                cover.add(vertexNames.get(i));
            }
        }

        long elapsed = System.nanoTime() - startTime;
        return new CoverResult("Exact Optimal (Exponential)", cover, cover.size(), 1.0, elapsed);
    }

    public ComparisonResult compareAll() {
        CoverResult exact = solveExact();
        CoverResult approx2 = solve2Approximation();
        CoverResult greedy = solveGreedy();

        if (exact.coverSize > 0) {
            approx2.approximationRatio = Math.round(((double) approx2.coverSize / exact.coverSize) * 100.0) / 100.0;
            greedy.approximationRatio = Math.round(((double) greedy.coverSize / exact.coverSize) * 100.0) / 100.0;
        }

        String reduction = "Reduction Pipeline: 3-SAT \u2192 CLIQUE (triplet gadget) \u2192 INDEPENDENT-SET (graph complement G') \u2192 VERTEX-COVER (V \\ S property). "
                + "In SmartLibrary, conflicting documents form edges. A Minimum Vertex Cover flags minimum documents to neutralize all conflict relationships.";

        return new ComparisonResult(approx2, greedy, exact, numVertices, edges.size(), reduction);
    }
}
