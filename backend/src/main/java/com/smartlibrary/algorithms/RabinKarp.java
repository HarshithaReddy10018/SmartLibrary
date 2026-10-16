package com.smartlibrary.algorithms;

import java.util.ArrayList;
import java.util.List;

/**
 * Module 1 (CO1): Rabin-Karp Rolling Hash Pattern Searching Algorithm.
 * Applied in SmartLibrary for hash-based document searching, citation signature checking, and plagiarism candidate screening.
 * Average Time: O(N + M), Worst-case: O(N * M), Space Complexity: O(1).
 */
public class RabinKarp {

    public static final int BASE = 256;
    public static final long PRIME = 1000000007L;

    public static class RKResult {
        public List<Integer> matchIndices;
        public int spuriousHits;
        public long patternHash;
        public long comparisons;
        public long executionTimeNanos;

        public RKResult(List<Integer> matchIndices, int spuriousHits, long patternHash, long comparisons, long executionTimeNanos) {
            this.matchIndices = matchIndices;
            this.spuriousHits = spuriousHits;
            this.patternHash = patternHash;
            this.comparisons = comparisons;
            this.executionTimeNanos = executionTimeNanos;
        }
    }

    /**
     * Searches for pattern in text using Rabin-Karp rolling hash.
     */
    public static RKResult search(String text, String pattern) {
        long startTime = System.nanoTime();
        List<Integer> matches = new ArrayList<>();
        int spuriousHits = 0;
        long comparisons = 0;

        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return new RKResult(matches, 0, 0L, 0, System.nanoTime() - startTime);
        }

        int n = text.length();
        int m = pattern.length();

        long pHash = 0;
        long tHash = 0;
        long h = 1;

        // The value of h would be "pow(BASE, m-1) % PRIME"
        for (int i = 0; i < m - 1; i++) {
            h = (h * BASE) % PRIME;
        }

        // Calculate initial hash value for pattern and first window of text
        for (int i = 0; i < m; i++) {
            pHash = (BASE * pHash + pattern.charAt(i)) % PRIME;
            tHash = (BASE * tHash + text.charAt(i)) % PRIME;
        }

        // Slide the pattern over text one by one
        for (int i = 0; i <= n - m; i++) {
            comparisons++;
            if (pHash == tHash) {
                // Check characters one by one
                boolean match = true;
                for (int j = 0; j < m; j++) {
                    comparisons++;
                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        match = false;
                        spuriousHits++;
                        break;
                    }
                }
                if (match) {
                    matches.add(i);
                }
            }

            // Calculate hash value for next window of text
            if (i < n - m) {
                tHash = (BASE * (tHash - text.charAt(i) * h) + text.charAt(i + m)) % PRIME;
                if (tHash < 0) {
                    tHash = (tHash + PRIME);
                }
            }
        }

        long elapsed = System.nanoTime() - startTime;
        return new RKResult(matches, spuriousHits, pHash, comparisons, elapsed);
    }
}
