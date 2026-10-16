package com.smartlibrary.algorithms;

import java.util.ArrayList;
import java.util.List;

/**
 * Module 1 (CO1): Knuth-Morris-Pratt (KMP) Pattern Searching Algorithm.
 * Applied in SmartLibrary for high-speed technical keyword matching in research paper abstracts.
 * Time Complexity: O(N + M), Space Complexity: O(M) where N = text length, M = pattern length.
 */
public class KMP {

    public static class KMPResult {
        public List<Integer> matchIndices;
        public int[] lps;
        public long comparisons;
        public long executionTimeNanos;

        public KMPResult(List<Integer> matchIndices, int[] lps, long comparisons, long executionTimeNanos) {
            this.matchIndices = matchIndices;
            this.lps = lps;
            this.comparisons = comparisons;
            this.executionTimeNanos = executionTimeNanos;
        }
    }

    /**
     * Computes the Longest Proper Prefix which is also Suffix (LPS array).
     */
    public static int[] computeLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0;
        int i = 1;
        lps[0] = 0;

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }

    /**
     * Searches for pattern in text using KMP.
     */
    public static KMPResult search(String text, String pattern) {
        long startTime = System.nanoTime();
        List<Integer> matches = new ArrayList<>();
        long comparisons = 0;

        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return new KMPResult(matches, new int[0], 0, System.nanoTime() - startTime);
        }

        int[] lps = computeLPS(pattern);
        int n = text.length();
        int m = pattern.length();
        int i = 0; // index for text
        int j = 0; // index for pattern

        while (i < n) {
            comparisons++;
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
            }

            if (j == m) {
                matches.add(i - j);
                j = lps[j - 1];
            } else if (i < n && pattern.charAt(j) != text.charAt(i)) {
                comparisons++;
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        long elapsed = System.nanoTime() - startTime;
        return new KMPResult(matches, lps, comparisons, elapsed);
    }
}
