package com.smartlibrary.algorithms;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Module 6 (CO6): Randomized QuickSort.
 * Applied in SmartLibrary for ranking and sorting research papers by citations, publication year, or relevance scores.
 * Expected Time: O(N log N), Eliminates O(N^2) adversarial worst-case on pre-sorted document feeds.
 */
public class RandomizedQuickSort {

    public static class Document {
        public int id;
        public String title;
        public String author;
        public int year;
        public int citations;
        public double relevanceScore;

        public Document(int id, String title, String author, int year, int citations, double relevanceScore) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.year = year;
            this.citations = citations;
            this.relevanceScore = relevanceScore;
        }

        public Document cloneDoc() {
            return new Document(id, title, author, year, citations, relevanceScore);
        }
    }

    public static class SortMetrics {
        public long comparisons = 0;
        public long swaps = 0;
        public int maxRecursionDepth = 0;
        public long executionTimeNanos = 0;
    }

    public static class BenchmarkResult {
        public Document[] sortedByCitations;
        public SortMetrics randomizedMetrics;
        public SortMetrics deterministicMetrics;
        public int datasetSize;

        public BenchmarkResult(Document[] sortedByCitations, SortMetrics randomizedMetrics, SortMetrics deterministicMetrics, int datasetSize) {
            this.sortedByCitations = sortedByCitations;
            this.randomizedMetrics = randomizedMetrics;
            this.deterministicMetrics = deterministicMetrics;
            this.datasetSize = datasetSize;
        }
    }

    private static final Random random = new Random();

    /**
     * Sorts documents using Randomized QuickSort.
     */
    public static SortMetrics sortRandomized(Document[] array, Comparator<Document> comparator) {
        SortMetrics metrics = new SortMetrics();
        long start = System.nanoTime();
        if (array != null && array.length > 1) {
            quickSort(array, 0, array.length - 1, comparator, true, metrics, 1);
        }
        metrics.executionTimeNanos = System.nanoTime() - start;
        return metrics;
    }

    /**
     * Sorts documents using Deterministic QuickSort (fixed first/last pivot).
     */
    public static SortMetrics sortDeterministic(Document[] array, Comparator<Document> comparator) {
        SortMetrics metrics = new SortMetrics();
        long start = System.nanoTime();
        if (array != null && array.length > 1) {
            quickSort(array, 0, array.length - 1, comparator, false, metrics, 1);
        }
        metrics.executionTimeNanos = System.nanoTime() - start;
        return metrics;
    }

    private static void quickSort(Document[] arr, int low, int high, Comparator<Document> comp, boolean isRandom, SortMetrics metrics, int depth) {
        metrics.maxRecursionDepth = Math.max(metrics.maxRecursionDepth, depth);
        if (low < high) {
            int pi = partition(arr, low, high, comp, isRandom, metrics);
            quickSort(arr, low, pi - 1, comp, isRandom, metrics, depth + 1);
            quickSort(arr, pi + 1, high, comp, isRandom, metrics, depth + 1);
        }
    }

    private static int partition(Document[] arr, int low, int high, Comparator<Document> comp, boolean isRandom, SortMetrics metrics) {
        if (isRandom) {
            int pivotIndex = low + random.nextInt(high - low + 1);
            swap(arr, pivotIndex, high, metrics);
        }

        Document pivot = arr[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            metrics.comparisons++;
            if (comp.compare(arr[j], pivot) <= 0) {
                i++;
                swap(arr, i, j, metrics);
            }
        }
        swap(arr, i + 1, high, metrics);
        return i + 1;
    }

    private static void swap(Document[] arr, int i, int j, SortMetrics metrics) {
        if (i == j) return;
        metrics.swaps++;
        Document temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public static BenchmarkResult runComparativeBenchmark(Document[] sampleDocs) {
        Document[] randCopy = Arrays.stream(sampleDocs).map(Document::cloneDoc).toArray(Document[]::new);
        Document[] detCopy = Arrays.stream(sampleDocs).map(Document::cloneDoc).toArray(Document[]::new);

        Comparator<Document> byCitationsDesc = (a, b) -> Integer.compare(b.citations, a.citations);

        SortMetrics randMetrics = sortRandomized(randCopy, byCitationsDesc);
        SortMetrics detMetrics = sortDeterministic(detCopy, byCitationsDesc);

        return new BenchmarkResult(randCopy, randMetrics, detMetrics, sampleDocs.length);
    }
}
