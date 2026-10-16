package com.smartlibrary.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Module 3 (CO3): Bitmask Dynamic Programming.
 * Applied in SmartLibrary for selecting optimal document/resource combinations to cover research topic domains
 * at minimum acquisition cost or optimal workflow scheduling.
 * Time Complexity: O(M * 2^N), Space Complexity: O(2^N) where N = number of topics (<= 20), M = available resources.
 */
public class BitmaskDP {

    public static class ResourceBundle {
        public int id;
        public String title;
        public int cost;
        public List<String> coveredTopics;
        public int topicMask;

        public ResourceBundle(int id, String title, int cost, List<String> coveredTopics) {
            this.id = id;
            this.title = title;
            this.cost = cost;
            this.coveredTopics = coveredTopics;
            this.topicMask = 0;
        }
    }

    public static class SelectionResult {
        public int minCost;
        public List<ResourceBundle> selectedBundles;
        public int totalTopicsCovered;
        public boolean fullyCovered;
        public long executionTimeNanos;

        public SelectionResult(int minCost, List<ResourceBundle> selectedBundles, int totalTopicsCovered, boolean fullyCovered, long executionTimeNanos) {
            this.minCost = minCost;
            this.selectedBundles = selectedBundles;
            this.totalTopicsCovered = totalTopicsCovered;
            this.fullyCovered = fullyCovered;
            this.executionTimeNanos = executionTimeNanos;
        }
    }

    /**
     * Solves the minimum cost topic coverage problem using Bitmask DP.
     */
    public static SelectionResult selectOptimalBundles(List<String> allTopics, List<ResourceBundle> bundles) {
        long startTime = System.nanoTime();
        int n = allTopics.size();
        if (n > 20) {
            throw new IllegalArgumentException("Topic count exceeds bitmask capacity (max 20).");
        }

        // Map topic string to bit position
        for (ResourceBundle bundle : bundles) {
            int mask = 0;
            for (String topic : bundle.coveredTopics) {
                int idx = allTopics.indexOf(topic);
                if (idx >= 0) {
                    mask |= (1 << idx);
                }
            }
            bundle.topicMask = mask;
        }

        int targetMask = (1 << n) - 1;
        int[] dp = new int[1 << n];
        int[] parentMask = new int[1 << n];
        int[] parentBundleIdx = new int[1 << n];

        final int INF = 1_000_000_000;
        Arrays.fill(dp, INF);
        Arrays.fill(parentMask, -1);
        Arrays.fill(parentBundleIdx, -1);
        dp[0] = 0;

        for (int mask = 0; mask < (1 << n); mask++) {
            if (dp[mask] == INF) continue;

            for (int i = 0; i < bundles.size(); i++) {
                ResourceBundle b = bundles.get(i);
                int nextMask = mask | b.topicMask;
                int newCost = dp[mask] + b.cost;

                if (newCost < dp[nextMask]) {
                    dp[nextMask] = newCost;
                    parentMask[nextMask] = mask;
                    parentBundleIdx[nextMask] = i;
                }
            }
        }

        List<ResourceBundle> selected = new ArrayList<>();
        int curr = targetMask;
        boolean possible = dp[targetMask] < INF;

        if (!possible) {
            // Find max covered mask if full coverage is impossible
            int bestMask = 0;
            for (int m = 0; m < (1 << n); m++) {
                if (dp[m] < INF && Integer.bitCount(m) > Integer.bitCount(bestMask)) {
                    bestMask = m;
                }
            }
            curr = bestMask;
        }

        int totalCovered = Integer.bitCount(curr);
        int finalCost = dp[curr];

        while (curr > 0 && parentBundleIdx[curr] != -1) {
            int bIdx = parentBundleIdx[curr];
            selected.add(bundles.get(bIdx));
            curr = parentMask[curr];
        }

        long elapsed = System.nanoTime() - startTime;
        return new SelectionResult(finalCost, selected, totalCovered, possible, elapsed);
    }
}
