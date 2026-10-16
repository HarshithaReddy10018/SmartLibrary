package com.smartlibrary.algorithms;

import java.util.*;

/**
 * Module 4 (CO4): Maximum Bipartite Matching and König's Theorem.
 * Applied in SmartLibrary for assigning submitted research papers to qualified peer reviewers without conflicts of interest.
 * Demonstrates König's Theorem: Size of Maximum Matching = Size of Minimum Vertex Cover.
 * Time Complexity: O(V * E) / Hopcroft-Karp O(E * sqrt(V)).
 */
public class BipartiteMatching {

    public static class Assignment {
        public String paper;
        public String reviewer;

        public Assignment(String paper, String reviewer) {
            this.paper = paper;
            this.reviewer = reviewer;
        }
    }

    public static class MatchingResult {
        public int maxMatchingSize;
        public List<Assignment> assignments;
        public List<String> unassignedPapers;
        public List<String> availableReviewers;
        public List<String> konigMinVertexCover;
        public long executionTimeNanos;

        public MatchingResult(int maxMatchingSize, List<Assignment> assignments, List<String> unassignedPapers,
                              List<String> availableReviewers, List<String> konigMinVertexCover, long executionTimeNanos) {
            this.maxMatchingSize = maxMatchingSize;
            this.assignments = assignments;
            this.unassignedPapers = unassignedPapers;
            this.availableReviewers = availableReviewers;
            this.konigMinVertexCover = konigMinVertexCover;
            this.executionTimeNanos = executionTimeNanos;
        }
    }

    private final List<String> papers;
    private final List<String> reviewers;
    private final Map<Integer, List<Integer>> adj;

    public BipartiteMatching(List<String> papers, List<String> reviewers) {
        this.papers = papers;
        this.reviewers = reviewers;
        this.adj = new HashMap<>();
        for (int i = 0; i < papers.size(); i++) {
            adj.put(i, new ArrayList<>());
        }
    }

    public void addEligibility(int paperIdx, int reviewerIdx) {
        adj.get(paperIdx).add(reviewerIdx);
    }

    /**
     * Augmenting path algorithm (Kuhn's Algorithm) for Maximum Bipartite Matching.
     */
    public MatchingResult solve() {
        long startTime = System.nanoTime();
        int n = papers.size();
        int m = reviewers.size();

        int[] matchReviewerToPaper = new int[m];
        Arrays.fill(matchReviewerToPaper, -1);

        int[] matchPaperToReviewer = new int[n];
        Arrays.fill(matchPaperToReviewer, -1);

        int matchCount = 0;
        for (int u = 0; u < n; u++) {
            boolean[] visited = new boolean[m];
            if (dfs(u, visited, matchReviewerToPaper)) {
                matchCount++;
            }
        }

        // Fill paper to reviewer mapping
        for (int r = 0; r < m; r++) {
            if (matchReviewerToPaper[r] != -1) {
                matchPaperToReviewer[matchReviewerToPaper[r]] = r;
            }
        }

        List<Assignment> assignments = new ArrayList<>();
        List<String> unassigned = new ArrayList<>();

        for (int p = 0; p < n; p++) {
            if (matchPaperToReviewer[p] != -1) {
                assignments.add(new Assignment(papers.get(p), reviewers.get(matchPaperToReviewer[p])));
            } else {
                unassigned.add(papers.get(p));
            }
        }

        List<String> idleReviewers = new ArrayList<>();
        for (int r = 0; r < m; r++) {
            if (matchReviewerToPaper[r] == -1) {
                idleReviewers.add(reviewers.get(r));
            }
        }

        // König's Theorem: Finding Minimum Vertex Cover
        // 1. Mark all unmatched vertices in U (papers)
        // 2. Follow alternating paths: unmatched edges U->V and matched edges V->U
        // 3. Min Vertex Cover = (U \ Marked) U (V \cap Marked)
        boolean[] markedU = new boolean[n];
        boolean[] markedV = new boolean[m];
        Queue<Integer> queue = new ArrayDeque<>();

        for (int u = 0; u < n; u++) {
            if (matchPaperToReviewer[u] == -1) {
                markedU[u] = true;
                queue.add(u);
            }
        }

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : adj.get(u)) {
                if (!markedV[v] && matchPaperToReviewer[u] != v) {
                    markedV[v] = true;
                    int uNext = matchReviewerToPaper[v];
                    if (uNext != -1 && !markedU[uNext]) {
                        markedU[uNext] = true;
                        queue.add(uNext);
                    }
                }
            }
        }

        List<String> konigCover = new ArrayList<>();
        // Unmarked nodes from U
        for (int u = 0; u < n; u++) {
            if (!markedU[u]) {
                konigCover.add("Paper: " + papers.get(u));
            }
        }
        // Marked nodes from V
        for (int v = 0; v < m; v++) {
            if (markedV[v]) {
                konigCover.add("Reviewer: " + reviewers.get(v));
            }
        }

        long elapsed = System.nanoTime() - startTime;
        return new MatchingResult(matchCount, assignments, unassigned, idleReviewers, konigCover, elapsed);
    }

    private boolean dfs(int u, boolean[] visited, int[] matchReviewerToPaper) {
        for (int v : adj.get(u)) {
            if (!visited[v]) {
                visited[v] = true;
                if (matchReviewerToPaper[v] < 0 || dfs(matchReviewerToPaper[v], visited, matchReviewerToPaper)) {
                    matchReviewerToPaper[v] = u;
                    return true;
                }
            }
        }
        return false;
    }
}
