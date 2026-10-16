package com.smartlibrary.algorithms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Module 6 (CO6): Reservoir Sampling (Algorithm R).
 * Applied in SmartLibrary for continuous library-usage streams, streaming search queries,
 * and maintaining an unbiased random sample of size K from an unbounded log stream.
 * Time Complexity: O(N) single-pass stream processing, Space Complexity: O(K).
 */
public class ReservoirSampling {

    public static class StreamEvent {
        public long timestamp;
        public String userId;
        public String queryOrAction;
        public String documentTitle;

        public StreamEvent(long timestamp, String userId, String queryOrAction, String documentTitle) {
            this.timestamp = timestamp;
            this.userId = userId;
            this.queryOrAction = queryOrAction;
            this.documentTitle = documentTitle;
        }
    }

    public static class SamplingResult<T> {
        public List<T> sampledReservoir;
        public int streamTotalCount;
        public int reservoirCapacity;
        public double selectionProbability;
        public long executionTimeNanos;

        public SamplingResult(List<T> sampledReservoir, int streamTotalCount, int reservoirCapacity,
                              double selectionProbability, long executionTimeNanos) {
            this.sampledReservoir = sampledReservoir;
            this.streamTotalCount = streamTotalCount;
            this.reservoirCapacity = reservoirCapacity;
            this.selectionProbability = selectionProbability;
            this.executionTimeNanos = executionTimeNanos;
        }
    }

    private static final Random random = new Random();

    /**
     * Samples K elements uniformly at random from an iterator stream.
     */
    public static <T> SamplingResult<T> sample(Iterator<T> stream, int k) {
        long startTime = System.nanoTime();
        List<T> reservoir = new ArrayList<>(k);
        int count = 0;

        while (stream.hasNext()) {
            T item = stream.next();
            count++;

            if (reservoir.size() < k) {
                reservoir.add(item);
            } else {
                // Pick a random index between 0 and count-1 inclusive
                int j = random.nextInt(count);
                if (j < k) {
                    reservoir.set(j, item);
                }
            }
        }

        double prob = count == 0 ? 0.0 : Math.min(1.0, (double) k / count);
        long elapsed = System.nanoTime() - startTime;
        return new SamplingResult<>(reservoir, count, k, prob, elapsed);
    }

    /**
     * Demonstrates reservoir sampling on a simulated stream of library user search queries.
     */
    public static SamplingResult<StreamEvent> simulateQueryStream(List<StreamEvent> fullStream, int reservoirSize) {
        return sample(fullStream.iterator(), reservoirSize);
    }
}
