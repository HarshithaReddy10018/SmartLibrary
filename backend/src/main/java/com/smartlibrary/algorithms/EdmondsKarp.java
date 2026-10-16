package com.smartlibrary.algorithms;

import java.util.*;

/**
 * Module 4 (CO4): Edmonds-Karp Algorithm (BFS-based Ford-Fulkerson).
 * Applied in SmartLibrary for resource and physical book distribution across regional branch libraries.
 * Time Complexity: O(V * E^2), Space Complexity: O(V^2).
 */
public class EdmondsKarp {

    public static class FlowEdge {
        public int from;
        public int to;
        public int capacity;
        public int flow;

        public FlowEdge(int from, int to, int capacity, int flow) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.flow = flow;
        }
    }

    public static class MaxFlowResult {
        public int maxFlow;
        public List<FlowEdge> edges;
        public List<Integer> sourcePartition;
        public List<Integer> sinkPartition;
        public List<FlowEdge> minCutBottlenecks;
        public long executionTimeNanos;

        public MaxFlowResult(int maxFlow, List<FlowEdge> edges, List<Integer> sourcePartition,
                             List<Integer> sinkPartition, List<FlowEdge> minCutBottlenecks, long executionTimeNanos) {
            this.maxFlow = maxFlow;
            this.edges = edges;
            this.sourcePartition = sourcePartition;
            this.sinkPartition = sinkPartition;
            this.minCutBottlenecks = minCutBottlenecks;
            this.executionTimeNanos = executionTimeNanos;
        }
    }

    private final int vertices;
    private final int[][] capacity;
    private final int[][] flow;
    private final List<FlowEdge> edgeList;

    public EdmondsKarp(int vertices) {
        this.vertices = vertices;
        this.capacity = new int[vertices][vertices];
        this.flow = new int[vertices][vertices];
        this.edgeList = new ArrayList<>();
    }

    public void addEdge(int u, int v, int cap) {
        capacity[u][v] += cap;
        edgeList.add(new FlowEdge(u, v, cap, 0));
    }

    public MaxFlowResult computeMaxFlow(int source, int sink) {
        long startTime = System.nanoTime();
        int maxFlow = 0;
        int[] parent = new int[vertices];

        while (bfs(source, sink, parent)) {
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, capacity[u][v] - flow[u][v]);
            }

            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                flow[u][v] += pathFlow;
                flow[v][u] -= pathFlow;
            }

            maxFlow += pathFlow;
        }

        // Identify Min-Cut via BFS on residual graph
        boolean[] visited = new boolean[vertices];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(source);
        visited[source] = true;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v = 0; v < vertices; v++) {
                if (!visited[v] && capacity[u][v] - flow[u][v] > 0) {
                    visited[v] = true;
                    queue.add(v);
                }
            }
        }

        List<Integer> sPart = new ArrayList<>();
        List<Integer> tPart = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            if (visited[i]) sPart.add(i);
            else tPart.add(i);
        }

        List<FlowEdge> minCutEdges = new ArrayList<>();
        List<FlowEdge> activeEdges = new ArrayList<>();

        for (FlowEdge e : edgeList) {
            e.flow = flow[e.from][e.to];
            activeEdges.add(new FlowEdge(e.from, e.to, e.capacity, e.flow));
            if (visited[e.from] && !visited[e.to]) {
                minCutEdges.add(new FlowEdge(e.from, e.to, e.capacity, e.flow));
            }
        }

        long elapsed = System.nanoTime() - startTime;
        return new MaxFlowResult(maxFlow, activeEdges, sPart, tPart, minCutEdges, elapsed);
    }

    private boolean bfs(int s, int t, int[] parent) {
        Arrays.fill(parent, -1);
        parent[s] = -2;
        Queue<Integer> q = new ArrayDeque<>();
        q.add(s);

        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v = 0; v < vertices; v++) {
                if (parent[v] == -1 && capacity[u][v] - flow[u][v] > 0) {
                    parent[v] = u;
                    if (v == t) return true;
                    q.add(v);
                }
            }
        }
        return false;
    }
}
