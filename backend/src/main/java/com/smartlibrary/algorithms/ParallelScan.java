package com.smartlibrary.algorithms;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Module 6 (CO6): Blelloch Parallel Scan (Prefix Sum).
 * Applied in SmartLibrary for cumulative document usage analytics, timeline trend modeling,
 * and high-throughput parallel stream indexing.
 * Work Complexity: O(N) additions, Span / Critical Path: O(log N) depth.
 */
public class ParallelScan {

    private static final int THRESHOLD = 4096;
    private static final ForkJoinPool pool = ForkJoinPool.commonPool();

    public static class ScanResult {
        public long[] original;
        public long[] prefixSums;
        public long sequentialNanos;
        public long parallelNanos;
        public double speedup;
        public int treeDepth;

        public ScanResult(long[] original, long[] prefixSums, long sequentialNanos, long parallelNanos, double speedup, int treeDepth) {
            this.original = original;
            this.prefixSums = prefixSums;
            this.sequentialNanos = sequentialNanos;
            this.parallelNanos = parallelNanos;
            this.speedup = speedup;
            this.treeDepth = treeDepth;
        }
    }

    /**
     * Sequential prefix sum computation.
     */
    public static long[] sequentialPrefixSum(long[] array) {
        long[] result = new long[array.length];
        if (array.length == 0) return result;
        result[0] = array[0];
        for (int i = 1; i < array.length; i++) {
            result[i] = result[i - 1] + array[i];
        }
        return result;
    }

    /**
     * Blelloch Parallel Scan implementation using ForkJoin Up-sweep and Down-sweep.
     * Computes inclusive prefix sum.
     */
    public static ScanResult blellochParallelScan(long[] array) {
        int n = array.length;
        if (n == 0) {
            return new ScanResult(new long[0], new long[0], 0, 0, 1.0, 0);
        }

        // Measure Sequential
        long seqStart = System.nanoTime();
        long[] seqRes = sequentialPrefixSum(array);
        long seqElapsed = System.nanoTime() - seqStart;

        // Measure Blelloch Scan
        long parStart = System.nanoTime();
        // Pad to next power of 2 for clean tree structure
        int power = 1;
        while (power < n) power <<= 1;

        long[] tree = new long[2 * power];
        System.arraycopy(array, 0, tree, power, n);

        // Up-sweep (reduce phase)
        for (int d = power / 2; d >= 1; d /= 2) {
            int stride = d;
            pool.invoke(new RecursiveAction() {
                @Override
                protected void compute() {
                    for (int i = stride; i < 2 * stride; i++) {
                        tree[i] = tree[2 * i] + tree[2 * i + 1];
                    }
                }
            });
        }

        // Down-sweep phase (for exclusive scan, root set to 0)
        long totalSum = tree[1];
        tree[1] = 0;

        for (int d = 1; d < power; d *= 2) {
            int stride = d;
            pool.invoke(new RecursiveAction() {
                @Override
                protected void compute() {
                    for (int i = stride; i < 2 * stride; i++) {
                        long t = tree[2 * i];
                        tree[2 * i] = tree[i];
                        tree[2 * i + 1] = tree[i] + t;
                    }
                }
            });
        }

        // Convert exclusive to inclusive and extract back
        long[] parallelResult = new long[n];
        for (int i = 0; i < n; i++) {
            parallelResult[i] = tree[power + i] + array[i];
        }
        long parElapsed = System.nanoTime() - parStart;

        double speedup = parElapsed == 0 ? 1.0 : (double) seqElapsed / parElapsed;
        int depth = (int) (Math.log(power) / Math.log(2));

        return new ScanResult(
                Arrays.copyOf(array, Math.min(20, array.length)),
                parallelResult,
                seqElapsed,
                parElapsed,
                Math.round(speedup * 100.0) / 100.0,
                depth
        );
    }
}
