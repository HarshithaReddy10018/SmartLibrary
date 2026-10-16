package com.smartlibrary.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Module 2 (CO2): Suffix Array and Kasai's LCP (Longest Common Prefix) Array.
 * Applied in SmartLibrary for document indexing, binary-search substring retrieval, and detecting repeated common phrases.
 * Construction: O(N log^2 N) / O(N log N), Kasai LCP: O(N), Substring Query: O(M log N).
 */
public class SuffixArray {

    private final String text;
    private final int n;
    private final int[] suffixArray;
    private final int[] lcp;

    public static class SuffixItem {
        public int index;
        public int rank;
        public int nextRank;
    }

    public static class SuffixSearchResult {
        public boolean found;
        public int count;
        public List<Integer> occurrences;
        public long executionTimeNanos;

        public SuffixSearchResult(boolean found, int count, List<Integer> occurrences, long executionTimeNanos) {
            this.found = found;
            this.count = count;
            this.occurrences = occurrences;
            this.executionTimeNanos = executionTimeNanos;
        }
    }

    public static class RepeatedSubstringResult {
        public String substring;
        public int length;
        public int lcpIndex;

        public RepeatedSubstringResult(String substring, int length, int lcpIndex) {
            this.substring = substring;
            this.length = length;
            this.lcpIndex = lcpIndex;
        }
    }

    public SuffixArray(String text) {
        this.text = text;
        this.n = text.length();
        this.suffixArray = buildSuffixArray(text);
        this.lcp = buildKasaiLCP(text, this.suffixArray);
    }

    public int[] getSuffixArray() {
        return suffixArray;
    }

    public int[] getLcp() {
        return lcp;
    }

    public String getText() {
        return text;
    }

    /**
     * Builds Suffix Array using O(N log^2 N) prefix doubling algorithm.
     */
    private int[] buildSuffixArray(String s) {
        int len = s.length();
        SuffixItem[] suffixes = new SuffixItem[len];

        for (int i = 0; i < len; i++) {
            suffixes[i] = new SuffixItem();
            suffixes[i].index = i;
            suffixes[i].rank = s.charAt(i);
            suffixes[i].nextRank = (i + 1 < len) ? s.charAt(i + 1) : -1;
        }

        Arrays.sort(suffixes, (a, b) -> {
            if (a.rank != b.rank) return Integer.compare(a.rank, b.rank);
            return Integer.compare(a.nextRank, b.nextRank);
        });

        int[] ind = new int[len];
        for (int k = 4; k < 2 * len; k *= 2) {
            int rank = 0;
            int prevRank = suffixes[0].rank;
            suffixes[0].rank = rank;
            ind[suffixes[0].index] = 0;

            for (int i = 1; i < len; i++) {
                if (suffixes[i].rank == prevRank && suffixes[i].nextRank == suffixes[i - 1].nextRank) {
                    prevRank = suffixes[i].rank;
                    suffixes[i].rank = rank;
                } else {
                    prevRank = suffixes[i].rank;
                    suffixes[i].rank = ++rank;
                }
                ind[suffixes[i].index] = i;
            }

            for (int i = 0; i < len; i++) {
                int nextIndex = suffixes[i].index + k / 2;
                suffixes[i].nextRank = (nextIndex < len) ? suffixes[ind[nextIndex]].rank : -1;
            }

            Arrays.sort(suffixes, (a, b) -> {
                if (a.rank != b.rank) return Integer.compare(a.rank, b.rank);
                return Integer.compare(a.nextRank, b.nextRank);
            });
        }

        int[] sa = new int[len];
        for (int i = 0; i < len; i++) {
            sa[i] = suffixes[i].index;
        }
        return sa;
    }

    /**
     * Kasai's algorithm to compute LCP array in O(N) time.
     */
    private int[] buildKasaiLCP(String s, int[] sa) {
        int len = s.length();
        int[] lcpArr = new int[len];
        int[] invSa = new int[len];

        for (int i = 0; i < len; i++) {
            invSa[sa[i]] = i;
        }

        int k = 0;
        for (int i = 0; i < len; i++) {
            if (invSa[i] == len - 1) {
                k = 0;
                continue;
            }
            int j = sa[invSa[i] + 1];

            while (i + k < len && j + k < len && s.charAt(i + k) == s.charAt(j + k)) {
                k++;
            }
            lcpArr[invSa[i]] = k;

            if (k > 0) {
                k--;
            }
        }
        return lcpArr;
    }

    /**
     * Binary search to find pattern occurrences using the Suffix Array.
     */
    public SuffixSearchResult search(String pattern) {
        long start = System.nanoTime();
        List<Integer> occurrences = new ArrayList<>();
        if (pattern == null || pattern.isEmpty() || n == 0) {
            return new SuffixSearchResult(false, 0, occurrences, System.nanoTime() - start);
        }

        int m = pattern.length();
        // Lower bound
        int l = 0, r = n - 1;
        int first = -1;
        while (l <= r) {
            int mid = l + (r - l) / 2;
            int suffixStart = suffixArray[mid];
            int cmp = comparePrefix(pattern, text, suffixStart, m);
            if (cmp <= 0) {
                if (cmp == 0) first = mid;
                r = mid - 1;
            } else {
                l = mid + 1;
            }
        }

        if (first == -1) {
            return new SuffixSearchResult(false, 0, occurrences, System.nanoTime() - start);
        }

        // Upper bound
        l = first;
        r = n - 1;
        int last = first;
        while (l <= r) {
            int mid = l + (r - l) / 2;
            int suffixStart = suffixArray[mid];
            int cmp = comparePrefix(pattern, text, suffixStart, m);
            if (cmp == 0) {
                last = mid;
                l = mid + 1;
            } else {
                r = mid - 1;
            }
        }

        for (int i = first; i <= last; i++) {
            occurrences.add(suffixArray[i]);
        }

        return new SuffixSearchResult(true, occurrences.size(), occurrences, System.nanoTime() - start);
    }

    private int comparePrefix(String pattern, String text, int textStart, int patLen) {
        int textRemaining = text.length() - textStart;
        int limit = Math.min(patLen, textRemaining);
        for (int i = 0; i < limit; i++) {
            char pChar = pattern.charAt(i);
            char tChar = text.charAt(textStart + i);
            if (pChar != tChar) {
                return Character.compare(pChar, tChar);
            }
        }
        if (patLen <= textRemaining) return 0;
        return 1; // Pattern is longer than suffix
    }

    /**
     * Finds the longest repeated substring in the text using the LCP array.
     */
    public RepeatedSubstringResult findLongestRepeatedSubstring() {
        int maxLcp = 0;
        int idx = -1;

        for (int i = 0; i < n - 1; i++) {
            if (lcp[i] > maxLcp) {
                maxLcp = lcp[i];
                idx = i;
            }
        }

        if (maxLcp == 0 || idx == -1) {
            return new RepeatedSubstringResult("", 0, -1);
        }

        String sub = text.substring(suffixArray[idx], suffixArray[idx] + maxLcp);
        return new RepeatedSubstringResult(sub, maxLcp, idx);
    }
}
