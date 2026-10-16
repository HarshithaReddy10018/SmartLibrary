package com.smartlibrary.algorithms;

/**
 * Module 3 (CO3): Matrix-Chain Multiplication (MCM) Dynamic Programming.
 * Applied in SmartLibrary for optimizing sequential document-processing pipelines, matrix join projections,
 * and high-dimensional vector transformation cascades.
 * Time Complexity: O(K^3), Space Complexity: O(K^2) where K = number of matrices.
 */
public class MatrixChainMultiplication {

    public static class MCMResult {
        public int minCost;
        public String optimalOrder;
        public int[][] mTable;
        public int[][] sTable;
        public int[] dimensions;

        public MCMResult(int minCost, String optimalOrder, int[][] mTable, int[][] sTable, int[] dimensions) {
            this.minCost = minCost;
            this.optimalOrder = optimalOrder;
            this.mTable = mTable;
            this.sTable = sTable;
            this.dimensions = dimensions;
        }
    }

    /**
     * Solves the Matrix Chain Multiplication problem given an array of dimensions.
     * Matrix i has dimensions p[i-1] x p[i].
     */
    public static MCMResult solve(int[] p, String[] operationNames) {
        int n = p.length - 1; // number of matrices
        int[][] m = new int[n + 1][n + 1];
        int[][] s = new int[n + 1][n + 1];

        for (int i = 1; i <= n; i++) {
            m[i][i] = 0;
        }

        // L is the chain length
        for (int L = 2; L <= n; L++) {
            for (int i = 1; i <= n - L + 1; i++) {
                int j = i + L - 1;
                m[i][j] = Integer.MAX_VALUE;

                for (int k = i; k <= j - 1; k++) {
                    int q = m[i][k] + m[k + 1][j] + p[i - 1] * p[k] * p[j];
                    if (q < m[i][j]) {
                        m[i][j] = q;
                        s[i][j] = k;
                    }
                }
            }
        }

        StringBuilder order = new StringBuilder();
        buildOptimalString(s, 1, n, order, operationNames);

        return new MCMResult(m[1][n], order.toString(), m, s, p);
    }

    private static void buildOptimalString(int[][] s, int i, int j, StringBuilder sb, String[] names) {
        if (i == j) {
            if (names != null && i - 1 < names.length) {
                sb.append(names[i - 1]);
            } else {
                sb.append("Op").append(i);
            }
            return;
        }
        sb.append("(");
        buildOptimalString(s, i, s[i][j], sb, names);
        sb.append(" \u2A09 ");
        buildOptimalString(s, s[i][j] + 1, j, sb, names);
        sb.append(")");
    }
}
