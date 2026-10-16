package com.smartlibrary.algorithms;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Module 6 (CO6): Parallel Tree Reduction and Brent's Theorem.
 * Applied in SmartLibrary for calculating document statistics (total word counts, max citations, aggregate viewership)
 * using parallel divide-and-conquer over multi-core processors.
 * Work T_1: O(N), Span T_infinity: O(log N).
 * Brent's Theorem Bound: T_p <= (T_1 - T_infinity)/p + T_infinity.
 */
public class ParallelReduce {

    private static final int THRESHOLD = 5000;
    private static final ForkJoinPool pool = ForkJoinPool.commonPool();

    public static class ParallelStatsResult {
        public long totalSum;
        public long maxValue;
        public long sequentialNanos;
        public long parallelNanos;
        public double speedup;
        public int availableProcessors;
        public long theoreticalSpan;
        public double brentsPredictedSpeedup;

        public ParallelStatsResult(long totalSum, long maxValue, long sequentialNanos, long parallelNanos,
                                   double speedup, int availableProcessors, long theoreticalSpan, double brentsPredictedSpeedup) {
            this.totalSum = totalSum;
            this.maxValue = maxValue;
            this.sequentialNanos = sequentialNanos;
            this.parallelNanos = parallelNanos;
            this.speedup = speedup;
            this.availableProcessors = availableProcessors;
            this.theoreticalSpan = theoreticalSpan;
            this.brentsPredictedSpeedup = brentsPredictedSpeedup;
        }
    }

    private static class SumTask extends RecursiveTask<Long> {
        private final long[] data;
        private final int start;
        private final int end;

        public SumTask(long[] data, int start, int end) {
            this.data = data;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= THRESHOLD) {
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += data[i];
                }
                return sum;
            } else {
                int mid = start + (end - start) / 2;
                SumTask left = new SumTask(data, start, mid);
                SumTask right = new SumTask(data, mid, end);
                left.fork();
                long rightAns = right.compute();
                long leftAns = left.join();
                return leftAns + rightAns;
            }
        }
    }

    private static class MaxTask extends RecursiveTask<Long> {
        private final long[] data;
        private final int start;
        private final int end;

        public MaxTask(long[] data, int start, int end) {
            this.data = data;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= THRESHOLD) {
                long max = Long.MIN_VALUE;
                for (int i = start; i < end; i++) {
                    if (data[i] > max) max = data[i];
                }
                return max;
            } else {
                int mid = start + (end - start) / 2;
                MaxTask left = new MaxTask(data, start, mid);
                MaxTask right = new MaxTask(data, mid, end);
                left.fork();
                long rightAns = right.compute();
                long leftAns = left.join();
                return Math.max(leftAns, rightAns);
            }
        }
    }

    /**
     * Executes comparative sequential vs parallel reduction benchmarks and evaluates Brent's theorem.
     */
    public static ParallelStatsResult benchmarkArrayReduction(long[] array) {
        int n = array.length;
        int p = Runtime.getRuntime().availableProcessors();

        // 1. Sequential computation
        long seqStart = System.nanoTime();
        long seqSum = 0;
        long seqMax = Long.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            seqSum += array[i];
            if (array[i] > seqMax) seqMax = array[i];
        }
        long seqElapsed = System.nanoTime() - seqStart;

        // 2. Parallel ForkJoin computation
        long parStart = System.nanoTime();
        Long parSum = pool.invoke(new SumTask(array, 0, n));
        Long parMax = pool.invoke(new MaxTask(array, 0, n));
        long parElapsed = System.nanoTime() - parStart;

        double speedup = parElapsed == 0 ? 1.0 : (double) seqElapsed / parElapsed;

        // Theoretical Span = ceil(log2(N))
        long span = (long) Math.ceil(Math.log(n) / Math.log(2));

        // Brent's Theorem: T_p <= T_1 / p + T_infinity
        double brentTimeRatio = (1.0 / p) + ((double) span / Math.max(1, n));
        double brentSpeedup = 1.0 / brentTimeRatio;

        return new ParallelStatsResult(
                parSum,
                parMax,
                seqElapsed,
                parElapsed,
                Math.round(speedup * 100.0) / 100.0,
                p,
                span,
                Math.round(brentSpeedup * 100.0) / 100.0
        );
    }

    /**
     * Parallel document word count aggregation.
     */
    public static long parallelWordCount(List<String> documents) {
        return documents.parallelStream()
                .mapToLong(doc -> {
                    if (doc == null || doc.isEmpty()) return 0;
                    String[] tokens = doc.trim().split("\\s+");
                    return tokens.length;
                })
                .sum();
    }
}
