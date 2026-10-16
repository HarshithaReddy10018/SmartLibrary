package com.smartlibrary.algorithms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Module 2 (CO2): Suffix Automaton (DAWG - Directed Acyclic Word Graph).
 * Applied in SmartLibrary for O(M) substring retrieval, total distinct query vocabulary calculation, and index-free token lookup.
 * Construction: O(N) linear time and space, Query: O(M) time where M = query length.
 */
public class SuffixAutomaton {

    public static class State {
        public int len;
        public int link;
        public Map<Character, Integer> next = new HashMap<>();
        public boolean isClone = false;
        public int firstEndPos = -1;

        public State(int len, int link) {
            this.len = len;
            this.link = link;
        }
    }

    private final State[] states;
    private int sz;
    private int last;
    private final String text;

    public SuffixAutomaton(String text) {
        this.text = text;
        int maxStates = 2 * text.length() + 2;
        this.states = new State[maxStates];
        this.sz = 0;
        this.last = 0;

        states[0] = new State(0, -1);
        sz++;

        for (int i = 0; i < text.length(); i++) {
            extend(text.charAt(i), i);
        }
    }

    private void extend(char c, int index) {
        int cur = sz++;
        states[cur] = new State(states[last].len + 1, 0);
        states[cur].firstEndPos = index;

        int p = last;
        while (p != -1 && !states[p].next.containsKey(c)) {
            states[p].next.put(c, cur);
            p = states[p].link;
        }

        if (p == -1) {
            states[cur].link = 0;
        } else {
            int q = states[p].next.get(c);
            if (states[p].len + 1 == states[q].len) {
                states[cur].link = q;
            } else {
                int clone = sz++;
                states[clone] = new State(states[p].len + 1, states[q].link);
                states[clone].next.putAll(states[q].next);
                states[clone].isClone = true;
                states[clone].firstEndPos = states[q].firstEndPos;

                while (p != -1 && states[p].next.containsKey(c) && states[p].next.get(c) == q) {
                    states[p].next.put(c, clone);
                    p = states[p].link;
                }
                states[q].link = clone;
                states[cur].link = clone;
            }
        }
        last = cur;
    }

    /**
     * Checks whether the given pattern is a substring in O(M) time.
     */
    public boolean contains(String pattern) {
        if (pattern == null || pattern.isEmpty()) return true;
        int cur = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (!states[cur].next.containsKey(c)) {
                return false;
            }
            cur = states[cur].next.get(c);
        }
        return true;
    }

    /**
     * Finds the first occurrence index of the pattern in the original text in O(M) time.
     */
    public int findFirstOccurrence(String pattern) {
        if (pattern == null || pattern.isEmpty()) return 0;
        int cur = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (!states[cur].next.containsKey(c)) {
                return -1;
            }
            cur = states[cur].next.get(c);
        }
        return states[cur].firstEndPos - pattern.length() + 1;
    }

    /**
     * Calculates the total number of distinct substrings in O(states).
     */
    public long countDistinctSubstrings() {
        long[] dp = new long[sz];
        Arrays.fill(dp, -1);
        return countPaths(0, dp) - 1; // Exclude empty string
    }

    private long countPaths(int u, long[] dp) {
        if (dp[u] != -1) return dp[u];
        long ans = 1; // current substring
        for (int v : states[u].next.values()) {
            ans += countPaths(v, dp);
        }
        dp[u] = ans;
        return ans;
    }

    public int getStateCount() {
        return sz;
    }
}
