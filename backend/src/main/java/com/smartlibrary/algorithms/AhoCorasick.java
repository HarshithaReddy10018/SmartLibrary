package com.smartlibrary.algorithms;

import java.util.*;

/**
 * Module 1 (CO1): Aho-Corasick Multi-Pattern Search Algorithm.
 * Applied in SmartLibrary for simultaneously indexing and searching dozens of domain keywords across research repositories.
 * Time Complexity: O(N + sum(M_i) + K) where N = text length, M_i = keyword lengths, K = total matches.
 */
public class AhoCorasick {

    public static class Match {
        public String keyword;
        public int startIndex;
        public int endIndex;

        public Match(String keyword, int startIndex, int endIndex) {
            this.keyword = keyword;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }

    public static class ACResult {
        public List<Match> matches;
        public Map<String, Integer> keywordCount;
        public int trieNodeCount;
        public long executionTimeNanos;

        public ACResult(List<Match> matches, Map<String, Integer> keywordCount, int trieNodeCount, long executionTimeNanos) {
            this.matches = matches;
            this.keywordCount = keywordCount;
            this.trieNodeCount = trieNodeCount;
            this.executionTimeNanos = executionTimeNanos;
        }
    }

    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        Node fail = null;
        List<String> output = new ArrayList<>();
        int id;

        Node(int id) {
            this.id = id;
        }
    }

    private final Node root;
    private int nodeCounter = 0;
    private final List<String> keywords;

    public AhoCorasick(List<String> keywords) {
        this.root = new Node(nodeCounter++);
        this.keywords = new ArrayList<>(keywords);
        buildTrie();
        buildFailureLinks();
    }

    private void buildTrie() {
        for (String word : keywords) {
            if (word == null || word.isEmpty()) continue;
            Node current = root;
            for (char ch : word.toCharArray()) {
                current = current.children.computeIfAbsent(ch, c -> new Node(nodeCounter++));
            }
            current.output.add(word);
        }
    }

    private void buildFailureLinks() {
        Queue<Node> queue = new ArrayDeque<>();

        // Level 1 nodes fail to root
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
                char ch = entry.getKey();
                Node child = entry.getValue();

                // Find fail transition for child
                Node f = current.fail;
                while (f != null && !f.children.containsKey(ch)) {
                    f = f.fail;
                }
                child.fail = (f != null) ? f.children.get(ch) : root;

                // Inherit outputs from fail node
                child.output.addAll(child.fail.output);
                queue.add(child);
            }
        }
    }

    public ACResult search(String text) {
        long startTime = System.nanoTime();
        List<Match> matches = new ArrayList<>();
        Map<String, Integer> counts = new HashMap<>();
        for (String kw : keywords) {
            counts.put(kw, 0);
        }

        if (text == null || text.isEmpty()) {
            return new ACResult(matches, counts, nodeCounter, System.nanoTime() - startTime);
        }

        Node current = root;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            while (current != null && !current.children.containsKey(ch)) {
                current = current.fail;
            }

            if (current == null) {
                current = root;
                continue;
            }

            current = current.children.get(ch);
            for (String matchedWord : current.output) {
                int start = i - matchedWord.length() + 1;
                matches.add(new Match(matchedWord, start, i));
                counts.put(matchedWord, counts.getOrDefault(matchedWord, 0) + 1);
            }
        }

        long elapsed = System.nanoTime() - startTime;
        return new ACResult(matches, counts, nodeCounter, elapsed);
    }
}
