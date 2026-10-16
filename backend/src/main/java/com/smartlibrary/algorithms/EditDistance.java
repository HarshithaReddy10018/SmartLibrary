package com.smartlibrary.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Module 3 (CO3): Levenshtein and Damerau-Levenshtein Edit Distance Algorithms.
 * Applied in SmartLibrary for search query spell checking, typo tolerance, and "Did you mean?" suggestions.
 * Time Complexity: O(N * M), Space Complexity: O(N * M).
 */
public class EditDistance {

    public static class EditOperation {
        public String type; // "MATCH", "INSERT", "DELETE", "SUBSTITUTE", "TRANSPOSE"
        public char sourceChar;
        public char targetChar;
        public int position;

        public EditOperation(String type, char sourceChar, char targetChar, int position) {
            this.type = type;
            this.sourceChar = sourceChar;
            this.targetChar = targetChar;
            this.position = position;
        }
    }

    public static class EditResult {
        public int distance;
        public int[][] dpMatrix;
        public List<EditOperation> operations;
        public String query;
        public String target;

        public EditResult(int distance, int[][] dpMatrix, List<EditOperation> operations, String query, String target) {
            this.distance = distance;
            this.dpMatrix = dpMatrix;
            this.operations = operations;
            this.query = query;
            this.target = target;
        }
    }

    public static class Suggestion {
        public String candidate;
        public int distance;
        public double similarityPercentage;

        public Suggestion(String candidate, int distance, double similarityPercentage) {
            this.candidate = candidate;
            this.distance = distance;
            this.similarityPercentage = similarityPercentage;
        }
    }

    /**
     * Computes classic Levenshtein distance with full DP matrix and traceback.
     */
    public static EditResult levenshtein(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int insertOp = dp[i][j - 1] + 1;
                    int deleteOp = dp[i - 1][j] + 1;
                    int substituteOp = dp[i - 1][j - 1] + 1;
                    dp[i][j] = Math.min(insertOp, Math.min(deleteOp, substituteOp));
                }
            }
        }

        // Traceback operations
        List<EditOperation> ops = new ArrayList<>();
        int i = m, j = n;
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && s1.charAt(i - 1) == s2.charAt(j - 1)) {
                ops.add(new EditOperation("MATCH", s1.charAt(i - 1), s2.charAt(j - 1), i));
                i--;
                j--;
            } else if (i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] + 1) {
                ops.add(new EditOperation("SUBSTITUTE", s1.charAt(i - 1), s2.charAt(j - 1), i));
                i--;
                j--;
            } else if (j > 0 && dp[i][j] == dp[i][j - 1] + 1) {
                ops.add(new EditOperation("INSERT", ' ', s2.charAt(j - 1), i));
                j--;
            } else if (i > 0 && dp[i][j] == dp[i - 1][j] + 1) {
                ops.add(new EditOperation("DELETE", s1.charAt(i - 1), ' ', i));
                i--;
            } else {
                i--;
                j--;
            }
        }
        Collections.reverse(ops);

        return new EditResult(dp[m][n], dp, ops, s1, s2);
    }

    /**
     * Damerau-Levenshtein distance supporting character transpositions.
     */
    public static int damerauLevenshtein(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] d = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) d[i][0] = i;
        for (int j = 0; j <= n; j++) d[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                d[i][j] = Math.min(d[i - 1][j] + 1, // deletion
                          Math.min(d[i][j - 1] + 1, // insertion
                                   d[i - 1][j - 1] + cost)); // substitution

                if (i > 1 && j > 1 && s1.charAt(i - 1) == s2.charAt(j - 2) && s1.charAt(i - 2) == s2.charAt(j - 1)) {
                    d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1); // transposition
                }
            }
        }
        return d[m][n];
    }

    /**
     * Suggests the closest dictionary terms for a misspelled user query.
     */
    public static List<Suggestion> suggest(String query, List<String> dictionary, int maxSuggestions) {
        List<Suggestion> suggestions = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (String word : dictionary) {
            int dist = damerauLevenshtein(lowerQuery, word.toLowerCase().trim());
            int maxLen = Math.max(lowerQuery.length(), word.length());
            double similarity = maxLen == 0 ? 100.0 : ((1.0 - (double) dist / maxLen) * 100.0);
            suggestions.add(new Suggestion(word, dist, Math.round(similarity * 10.0) / 10.0));
        }

        suggestions.sort((a, b) -> {
            if (a.distance != b.distance) return Integer.compare(a.distance, b.distance);
            return Double.compare(b.similarityPercentage, a.similarityPercentage);
        });

        return suggestions.subList(0, Math.min(maxSuggestions, suggestions.size()));
    }
}
