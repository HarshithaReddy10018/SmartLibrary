package com.smartlibrary.algorithms;

import java.util.*;

/**
 * Module 4 (CO4): Dinic's Algorithm for Maximum Flow.
 * Applied in SmartLibrary for high-throughput digital resource allocation and high-scale campus network bandwidth routing.
 * Time Complexity: O(V^2 * E), Unit Networks: O(E * sqrt(V)), Space Complexity: O(V + E).
 */
public class Dinic {

    public static class Edge {
        public int to;
        public int rev;
        public int capacity;
        public int flow;

        public Edge(int to, int rev, int capacity, int flow) {
            this.to = to;
            this.rev = rev;
            this.capacity = capacity;
            this.flow = flow;
        }
    }

    public static class DinicFlowResult {
        public int maxFlow;
        public int levelGraphPhases;
        public long executionTimeNanos;

        public DinicFlowResult(int maxFlow, int levelGraphPhases, long executionTimeNanos) {
            this.maxFlow = maxFlow;
            this.levelGraphPhases = levelGraphPhases;
            this.executionTimeNanos = executionTimeNanos;
        }
    }

    private final int n;
    private final List<List<Edge>> graph;
    private final int[] level;
    private final int[] ptr;

    public Dinic(int n) {
        this.n = n;
        this.graph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }
        this.level = new int[n];
        this.ptr = new int[n];
    }

    public void addEdge(int from, int to, int capacity) {
        Edge a = new Edge(to, graph.get(to).size(), capacity, 0);
        Edge b = new Edge(from, graph.get(from).size(), 0, 0);
        graph.get(from).add(a);
        graph.get(to).add(b);
    }

    private boolean bfs(int s, int t) {
        Arrays.fill(level, -1);
        level[s] = 0;
        Queue<Integer> q = new ArrayDeque<>();
        q.add(s);

        while (!q.isEmpty()) {
            int v = q.poll();
            for (Edge edge : graph.get(v)) {
                if (edge.capacity - edge.flow > 0 && level[edge.to] == -1) {
                    level[edge.to] = level[v] + 1;
                    q.add(edge.to);
                }
            }
        }
        return level[t] != -1;
    }

    private int dfs(int v, int t, int pushed) {
        if (pushed == 0 || v == t) return pushed;

        for (int cid = ptr[v]; cid < graph.get(v).size(); cid++) {
            ptr[v] = cid;
            Edge edge = graph.get(v).get(cid);
            int tr = edge.to;

            if (level[v] + 1 != level[tr] || edge.capacity - edge.flow == 0) continue;

            int trPushed = dfs(tr, t, Math.min(pushed, edge.capacity - edge.flow));
            if (trPushed == 0) continue;

            edge.flow += trPushed;
            graph.get(tr).get(edge.rev).flow -= trPushed;
            return trPushed;
        }
        return 0;
    }

    public DinicFlowResult computeMaxFlow(int s, int t) {
        long startTime = System.nanoTime();
        int flow = 0;
        int phases = 0;

        while (bfs(s, t)) {
            phases++;
            Arrays.fill(ptr, 0);
            while (true) {
                int pushed = dfs(s, t, Integer.MAX_VALUE);
                if (pushed == 0) break;
                flow += pushed;
            }
        }

        long elapsed = System.nanoTime() - startTime;
        return new DinicFlowResult(flow, phases, elapsed);
    }
}
