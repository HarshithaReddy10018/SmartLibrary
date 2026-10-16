package com.smartlibrary.algorithms;

/**
 * Module 3 (CO3): Sequence Alignment Algorithms (Needleman-Wunsch & Smith-Waterman).
 * Applied in SmartLibrary for sequence-based document comparison, sentence-level plagiarism scoring, and citation matching.
 * Time Complexity: O(N * M), Space Complexity: O(N * M).
 */
public class SequenceAlignment {

    public static class AlignmentResult {
        public String mode; // "Global (Needleman-Wunsch)" or "Local (Smith-Waterman)"
        public int score;
        public String alignedSeq1;
        public String alignedSeq2;
        public String alignmentMarkers;
        public int[][] scoreMatrix;
        public double identityPercentage;

        public AlignmentResult(String mode, int score, String alignedSeq1, String alignedSeq2,
                               String alignmentMarkers, int[][] scoreMatrix, double identityPercentage) {
            this.mode = mode;
            this.score = score;
            this.alignedSeq1 = alignedSeq1;
            this.alignedSeq2 = alignedSeq2;
            this.alignmentMarkers = alignmentMarkers;
            this.scoreMatrix = scoreMatrix;
            this.identityPercentage = identityPercentage;
        }
    }

    /**
     * Needleman-Wunsch global alignment.
     */
    public static AlignmentResult needlemanWunsch(String s1, String s2, int matchScore, int mismatchPenalty, int gapPenalty) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i * gapPenalty;
        for (int j = 0; j <= n; j++) dp[0][j] = j * gapPenalty;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int match = dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? matchScore : mismatchPenalty);
                int delete = dp[i - 1][j] + gapPenalty;
                int insert = dp[i][j - 1] + gapPenalty;
                dp[i][j] = Math.max(match, Math.max(delete, insert));
            }
        }

        // Traceback
        StringBuilder a1 = new StringBuilder();
        StringBuilder a2 = new StringBuilder();
        StringBuilder markers = new StringBuilder();
        int i = m, j = n;
        int matches = 0;

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? matchScore : mismatchPenalty)) {
                a1.append(s1.charAt(i - 1));
                a2.append(s2.charAt(j - 1));
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    markers.append('|');
                    matches++;
                } else {
                    markers.append('.');
                }
                i--;
                j--;
            } else if (i > 0 && dp[i][j] == dp[i - 1][j] + gapPenalty) {
                a1.append(s1.charAt(i - 1));
                a2.append('-');
                markers.append(' ');
                i--;
            } else {
                a1.append('-');
                a2.append(s2.charAt(j - 1));
                markers.append(' ');
                j--;
            }
        }

        String res1 = a1.reverse().toString();
        String res2 = a2.reverse().toString();
        String markerStr = markers.reverse().toString();
        double identity = res1.isEmpty() ? 0.0 : ((double) matches / res1.length()) * 100.0;

        return new AlignmentResult("Global (Needleman-Wunsch)", dp[m][n], res1, res2, markerStr, dp,
                Math.round(identity * 10.0) / 10.0);
    }

    /**
     * Smith-Waterman local alignment.
     */
    public static AlignmentResult smithWaterman(String s1, String s2, int matchScore, int mismatchPenalty, int gapPenalty) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        int maxScore = 0;
        int maxI = 0, maxJ = 0;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int match = dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? matchScore : mismatchPenalty);
                int delete = dp[i - 1][j] + gapPenalty;
                int insert = dp[i][j - 1] + gapPenalty;
                dp[i][j] = Math.max(0, Math.max(match, Math.max(delete, insert)));

                if (dp[i][j] > maxScore) {
                    maxScore = dp[i][j];
                    maxI = i;
                    maxJ = j;
                }
            }
        }

        // Traceback from maximum cell until 0 is hit
        StringBuilder a1 = new StringBuilder();
        StringBuilder a2 = new StringBuilder();
        StringBuilder markers = new StringBuilder();
        int i = maxI, j = maxJ;
        int matches = 0;

        while (i > 0 && j > 0 && dp[i][j] > 0) {
            if (dp[i][j] == dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? matchScore : mismatchPenalty)) {
                a1.append(s1.charAt(i - 1));
                a2.append(s2.charAt(j - 1));
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    markers.append('|');
                    matches++;
                } else {
                    markers.append('.');
                }
                i--;
                j--;
            } else if (dp[i][j] == dp[i - 1][j] + gapPenalty) {
                a1.append(s1.charAt(i - 1));
                a2.append('-');
                markers.append(' ');
                i--;
            } else {
                a1.append('-');
                a2.append(s2.charAt(j - 1));
                markers.append(' ');
                j--;
            }
        }

        String res1 = a1.reverse().toString();
        String res2 = a2.reverse().toString();
        String markerStr = markers.reverse().toString();
        double identity = res1.isEmpty() ? 0.0 : ((double) matches / res1.length()) * 100.0;

        return new AlignmentResult("Local (Smith-Waterman)", maxScore, res1, res2, markerStr, dp,
                Math.round(identity * 10.0) / 10.0);
    }
}
