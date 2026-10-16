package com.smartlibrary.algorithms;

import java.util.ArrayList;
import java.util.List;

/**
 * Module 1 (CO1): Z-Function Algorithm.
 * Applied in SmartLibrary for detecting repeated phrases, duplicated excerpts, and fast pattern matching.
 * Time Complexity: O(N), Space Complexity: O(N).
 */
public class ZFunction {

    public static class ZResult {
        public List<Integer> matchIndices;
        public int[] zArray;
        public List<RepeatedPhrase> repeatedPhrases;
        public long comparisons;
        public long executionTimeNanos;

        public ZResult(List<Integer> matchIndices, int[] zArray, List<RepeatedPhrase> repeatedPhrases, long comparisons, long executionTimeNanos) {
            this.matchIndices = matchIndices;
            this.zArray = zArray;
            this.repeatedPhrases = repeatedPhrases;
            this.comparisons = comparisons;
            this.executionTimeNanos = executionTimeNanos;
        }
    }

    public static class RepeatedPhrase {
        public String phrase;
        public int length;
        public int position;

        public RepeatedPhrase(String phrase, int length, int position) {
            this.phrase = phrase;
            this.length = length;
            this.position = position;
        }
    }

    /**
     * Computes the Z-array for a string S.
     * Z[i] is length of the longest substring starting from s[i] which is also a prefix of S.
     */
    public static int[] calculateZ(String s) {
        int n = s.length();
        int[] z = new int[n];
        int l = 0, r = 0;

        for (int i = 1; i < n; i++) {
            if (i <= r) {
                z[i] = Math.min(r - i + 1, z[i - l]);
            }
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) {
                z[i]++;
            }
            if (i + z[i] - 1 > r) {
                l = i;
                r = i + z[i] - 1;
            }
        }
        return z;
    }

    /**
     * Searches for occurrences of pattern in text using the Z-algorithm.
     */
    public static ZResult search(String text, String pattern) {
        long startTime = System.nanoTime();
        List<Integer> matches = new ArrayList<>();
        List<RepeatedPhrase> repeated = new ArrayList<>();

        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return new ZResult(matches, new int[0], repeated, 0, System.nanoTime() - startTime);
        }

        String concat = pattern + "$" + text;
        int[] z = calculateZ(concat);
        int pLen = pattern.length();
        long comparisons = 0;

        for (int i = 0; i < z.length; i++) {
            comparisons++;
            if (z[i] == pLen) {
                int originalIndex = i - pLen - 1;
                if (originalIndex >= 0) {
                    matches.add(originalIndex);
                }
            }
        }

        // Detect repeated phrases within the text itself
        int[] textZ = calculateZ(text);
        for (int i = 1; i < textZ.length; i++) {
            if (textZ[i] >= 4) { // Phrases of 4+ characters repeated from the beginning
                int len = textZ[i];
                String sub = text.substring(i, Math.min(text.length(), i + len));
                repeated.add(new RepeatedPhrase(sub, len, i));
            }
        }

        long elapsed = System.nanoTime() - startTime;
        return new ZResult(matches, z, repeated, comparisons, elapsed);
    }
}
