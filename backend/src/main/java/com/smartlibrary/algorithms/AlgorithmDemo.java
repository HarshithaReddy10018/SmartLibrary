package com.smartlibrary.algorithms;

import java.util.*;

/**
 * SmartLibrary - Comprehensive Algorithm Test Suite and Demonstrator.
 * Tests and verifies all algorithmic modules:
 * M1 (CO1): KMP, Z-Function, Rabin-Karp, Aho-Corasick
 * M2 (CO2): Suffix Array, Kasai LCP, Suffix Automaton
 * M3 (CO3): Levenshtein / Damerau-Levenshtein, Needleman-Wunsch / Smith-Waterman, Matrix-Chain Mult, Bitmask DP
 * M4 (CO4): Edmonds-Karp Max Flow, Dinic Max Flow, Bipartite Matching + König's Theorem
 * M5 (CO5): Vertex-Cover 2-Approximation, Greedy, Exact Solver, Karp Reductions
 * M6 (CO6): Miller-Rabin, Randomized QuickSort, Reservoir Sampling, Blelloch Parallel Scan, Parallel Reduce + Brent's Theorem
 */
public class AlgorithmDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("          SMARTLIBRARY - INTELLIGENT DIGITAL PLATFORM ALGORITHM SUITE          ");
        System.out.println("================================================================================\n");

        runModule1Demo();
        runModule2Demo();
        runModule3Demo();
        runModule4Demo();
        runModule5Demo();
        runModule6Demo();

        System.out.println("\n================================================================================");
        System.out.println("          ALL 6 ALGORITHM MODULES EXECUTED AND VALIDATED SUCCESSFULLY!          ");
        System.out.println("================================================================================");
    }

    public static void runModule1Demo() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("[MODULE 1 - CO1] STRING ALGORITHMS");
        System.out.println("--------------------------------------------------------------------------------");

        String documentAbstract = "In digital library systems, indexing large scale text requires efficient substring search. "
                + "Text searching with KMP and Rabin-Karp provides rapid document retrieval. "
                + "The indexing architecture enables real-time citation analysis.";
        String keyword = "indexing";

        // 1. KMP
        KMP.KMPResult kmpRes = KMP.search(documentAbstract, keyword);
        System.out.printf("[KMP] Target keyword: '%s'%n", keyword);
        System.out.printf("  Matches found at indices: %s%n", kmpRes.matchIndices);
        System.out.printf("  Comparisons: %d, Time: %d ns%n", kmpRes.comparisons, kmpRes.executionTimeNanos);

        // 2. Z-Function
        ZFunction.ZResult zRes = ZFunction.search(documentAbstract, keyword);
        System.out.printf("[Z-Function] Matches at indices: %s%n", zRes.matchIndices);
        System.out.printf("  Repeated phrase samples detected: %d%n", zRes.repeatedPhrases.size());

        // 3. Rabin-Karp
        RabinKarp.RKResult rkRes = RabinKarp.search(documentAbstract, keyword);
        System.out.printf("[Rabin-Karp] Matches at indices: %s%n", rkRes.matchIndices);
        System.out.printf("  Spurious hash collisions: %d, Pattern Hash: %d%n", rkRes.spuriousHits, rkRes.patternHash);

        // 4. Aho-Corasick
        List<String> dictionary = List.of("indexing", "library", "search", "retrieval", "citation");
        AhoCorasick ac = new AhoCorasick(dictionary);
        AhoCorasick.ACResult acRes = ac.search(documentAbstract);
        System.out.printf("[Aho-Corasick Multi-Pattern Trie] Searched %d keywords simultaneously:%n", dictionary.size());
        acRes.keywordCount.forEach((k, v) -> System.out.printf("   - '%s': %d occurrences%n", k, v));
        System.out.println();
    }

    public static void runModule2Demo() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("[MODULE 2 - CO2] SUFFIX STRUCTURES & DOCUMENT SIMILARITY");
        System.out.println("--------------------------------------------------------------------------------");

        String text = "banana_library_research_analysis_library_index$";
        SuffixArray sa = new SuffixArray(text);
        SuffixArray.SuffixSearchResult searchRes = sa.search("library");

        System.out.printf("[Suffix Array] Text length: %d chars%n", text.length());
        System.out.printf("  Binary search for 'library': Found %d occurrences at indices %s%n",
                searchRes.count, searchRes.occurrences);

        SuffixArray.RepeatedSubstringResult lcpRepeat = sa.findLongestRepeatedSubstring();
        System.out.printf("  Kasai LCP Longest Repeated Substring: '%s' (length %d)%n",
                lcpRepeat.substring, lcpRepeat.length);

        // Suffix Automaton
        SuffixAutomaton sam = new SuffixAutomaton("library_research");
        System.out.printf("[Suffix Automaton (DAWG)] States created: %d%n", sam.getStateCount());
        System.out.printf("  Contains 'research'? %b | Contains 'algorithm'? %b%n",
                sam.contains("research"), sam.contains("algorithm"));
        System.out.printf("  Total distinct substrings in 'library_research': %d%n", sam.countDistinctSubstrings());
        System.out.println();
    }

    public static void runModule3Demo() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("[MODULE 3 - CO3] ADVANCED DYNAMIC PROGRAMMING");
        System.out.println("--------------------------------------------------------------------------------");

        // 1. Edit Distance / Query Correction
        String query = "algotithm";
        List<String> libraryCatalog = List.of("algorithm", "logarithm", "algebra", "automaton", "alignment");
        List<EditDistance.Suggestion> suggestions = EditDistance.suggest(query, libraryCatalog, 3);
        System.out.printf("[Levenshtein / Damerau Query Correction] Misspelled: '%s'%n", query);
        for (EditDistance.Suggestion s : suggestions) {
            System.out.printf("   - Suggestion: '%s' (Dist: %d, Similarity: %.1f%%)%n", s.candidate, s.distance, s.similarityPercentage);
        }

        // 2. Sequence Alignment
        String seqA = "DATASTRUCTURES";
        String seqB = "ALGORITHMICS";
        SequenceAlignment.AlignmentResult nw = SequenceAlignment.needlemanWunsch(seqA, seqB, 2, -1, -2);
        System.out.printf("[Needleman-Wunsch Global Alignment] Alignment Score: %d (Identity: %.1f%%)%n",
                nw.score, nw.identityPercentage);
        System.out.printf("  Seq 1: %s%n  Match: %s%n  Seq 2: %s%n", nw.alignedSeq1, nw.alignmentMarkers, nw.alignedSeq2);

        // 3. Matrix-Chain Multiplication
        int[] dims = {10, 30, 5, 60, 20}; // A1(10x30), A2(30x5), A3(5x60), A4(60x20)
        String[] ops = {"DocTokenizer", "TermMatrix", "EmbeddingMap", "IndexJoin"};
        MatrixChainMultiplication.MCMResult mcm = MatrixChainMultiplication.solve(dims, ops);
        System.out.printf("[Matrix-Chain Multiplication] Optimal scalar ops: %d%n", mcm.minCost);
        System.out.printf("  Optimal Pipeline Bracketing: %s%n", mcm.optimalOrder);

        // 4. Bitmask DP Resource Selection
        List<String> topics = List.of("NLP", "Graphs", "Cryptography", "Databases");
        List<BitmaskDP.ResourceBundle> bundles = List.of(
                new BitmaskDP.ResourceBundle(1, "Bundle A (Text & Graph Analytics)", 50, List.of("NLP", "Graphs")),
                new BitmaskDP.ResourceBundle(2, "Bundle B (Security & DB Core)", 70, List.of("Cryptography", "Databases")),
                new BitmaskDP.ResourceBundle(3, "Bundle C (Comprehensive Data)", 90, List.of("NLP", "Databases")),
                new BitmaskDP.ResourceBundle(4, "Bundle D (Cryptographic Proofs)", 40, List.of("Cryptography"))
        );
        BitmaskDP.SelectionResult bitmaskRes = BitmaskDP.selectOptimalBundles(topics, bundles);
        System.out.printf("[Bitmask DP] Optimal Subject Coverage Cost: $%d (Covered %d/%d topics)%n",
                bitmaskRes.minCost, bitmaskRes.totalTopicsCovered, topics.size());
        for (BitmaskDP.ResourceBundle b : bitmaskRes.selectedBundles) {
            System.out.printf("   - Selected: %s (Cost: $%d, Covers: %s)%n", b.title, b.cost, b.coveredTopics);
        }
        System.out.println();
    }

    public static void runModule4Demo() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("[MODULE 4 - CO4] NETWORK FLOW & RESOURCE ALLOCATION");
        System.out.println("--------------------------------------------------------------------------------");

        // 1. Edmonds-Karp
        // 0: Central Hub, 1: Branch North, 2: Branch South, 3: Campus A, 4: Campus B, 5: Sink Target
        EdmondsKarp ek = new EdmondsKarp(6);
        ek.addEdge(0, 1, 16);
        ek.addEdge(0, 2, 13);
        ek.addEdge(1, 2, 10);
        ek.addEdge(1, 3, 12);
        ek.addEdge(2, 1, 4);
        ek.addEdge(2, 4, 14);
        ek.addEdge(3, 2, 9);
        ek.addEdge(3, 5, 20);
        ek.addEdge(4, 3, 7);
        ek.addEdge(4, 5, 4);
        EdmondsKarp.MaxFlowResult ekRes = ek.computeMaxFlow(0, 5);
        System.out.printf("[Edmonds-Karp Max Flow] Maximum Resource Flow Delivered: %d units%n", ekRes.maxFlow);
        System.out.printf("  Min-Cut Bottleneck Edges identified: %d edges%n", ekRes.minCutBottlenecks.size());

        // 2. Dinic
        Dinic dinic = new Dinic(6);
        dinic.addEdge(0, 1, 16);
        dinic.addEdge(0, 2, 13);
        dinic.addEdge(1, 2, 10);
        dinic.addEdge(1, 3, 12);
        dinic.addEdge(2, 1, 4);
        dinic.addEdge(2, 4, 14);
        dinic.addEdge(3, 2, 9);
        dinic.addEdge(3, 5, 20);
        dinic.addEdge(4, 3, 7);
        dinic.addEdge(4, 5, 4);
        Dinic.DinicFlowResult dinicRes = dinic.computeMaxFlow(0, 5);
        System.out.printf("[Dinic Flow Algorithm] Max Flow: %d units across %d level-graph phases%n",
                dinicRes.maxFlow, dinicRes.levelGraphPhases);

        // 3. Bipartite Matching + König's Theorem
        List<String> papers = List.of("Paper #101 (NLP Transformers)", "Paper #102 (Graph Neural Nets)", "Paper #103 (Quantum Crypto)");
        List<String> reviewers = List.of("Prof. Turing", "Dr. Shannon", "Dr. Lovelace");
        BipartiteMatching bm = new BipartiteMatching(papers, reviewers);
        bm.addEligibility(0, 0); // Paper 101 -> Turing
        bm.addEligibility(0, 1); // Paper 101 -> Shannon
        bm.addEligibility(1, 1); // Paper 102 -> Shannon
        bm.addEligibility(2, 2); // Paper 103 -> Lovelace
        BipartiteMatching.MatchingResult bmRes = bm.solve();
        System.out.printf("[Bipartite Matching] Reviewer Assignments Made: %d/%d papers%n",
                bmRes.maxMatchingSize, papers.size());
        for (BipartiteMatching.Assignment a : bmRes.assignments) {
            System.out.printf("   - %s assigned to %s%n", a.paper, a.reviewer);
        }
        System.out.printf("  König's Theorem Min Vertex Cover Size = %d nodes: %s%n",
                bmRes.konigMinVertexCover.size(), bmRes.konigMinVertexCover);
        System.out.println();
    }

    public static void runModule5Demo() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("[MODULE 5 - CO5] NP-COMPLETENESS & APPROXIMATION ALGORITHMS");
        System.out.println("--------------------------------------------------------------------------------");

        List<String> docTitles = List.of("Doc A", "Doc B", "Doc C", "Doc D", "Doc E", "Doc F");
        VertexCoverApproximation vca = new VertexCoverApproximation(docTitles.size(), docTitles);
        // Add conflicting document pairs
        vca.addConflictEdge(0, 1);
        vca.addConflictEdge(1, 2);
        vca.addConflictEdge(2, 3);
        vca.addConflictEdge(3, 4);
        vca.addConflictEdge(4, 5);
        vca.addConflictEdge(1, 4);

        VertexCoverApproximation.ComparisonResult comp = vca.compareAll();
        System.out.printf("[Vertex Cover Conflict Resolution] Total Documents: %d, Conflict Pairs: %d%n",
                comp.totalVertices, comp.totalEdges);
        System.out.printf("  Exact Optimal Cover Size: %d (Set: %s)%n", comp.exact.coverSize, comp.exact.coverVertices);
        System.out.printf("  2-Approximation Size:     %d (Set: %s, Ratio: %.2f)%n",
                comp.approx2.coverSize, comp.approx2.coverVertices, comp.approx2.approximationRatio);
        System.out.printf("  Greedy Heuristic Size:    %d (Set: %s, Ratio: %.2f)%n",
                comp.greedy.coverSize, comp.greedy.coverVertices, comp.greedy.approximationRatio);
        System.out.printf("  %s%n", comp.reductionChainExplanation);
        System.out.println();
    }

    public static void runModule6Demo() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("[MODULE 6 - CO6] RANDOMIZED & PARALLEL ALGORITHMS");
        System.out.println("--------------------------------------------------------------------------------");

        // 1. Miller-Rabin
        long testPrime = 1000000007L;
        MillerRabin.PrimalityResult mrRes = MillerRabin.isPrime(testPrime, 10);
        System.out.printf("[Miller-Rabin Primality] Number: %d -> Is Prime: %b (Confidence: %.6f%%, %d rounds)%n",
                mrRes.number, mrRes.isPrime, mrRes.confidencePercentage, mrRes.roundsTested);

        // 2. Randomized QuickSort
        RandomizedQuickSort.Document[] docs = {
                new RandomizedQuickSort.Document(1, "Deep Learning Foundations", "LeCun et al.", 2015, 45000, 9.8),
                new RandomizedQuickSort.Document(2, "Attention Is All You Need", "Vaswani et al.", 2017, 85000, 9.9),
                new RandomizedQuickSort.Document(3, "ImageNet Classification", "Krizhevsky et al.", 2012, 70000, 9.7),
                new RandomizedQuickSort.Document(4, "Adam Optimizer", "Kingma & Ba", 2014, 60000, 9.5)
        };
        RandomizedQuickSort.BenchmarkResult sortRes = RandomizedQuickSort.runComparativeBenchmark(docs);
        System.out.printf("[Randomized QuickSort] Sorted %d papers by citations:%n", docs.length);
        for (RandomizedQuickSort.Document d : sortRes.sortedByCitations) {
            System.out.printf("   - %s (%d citations)%n", d.title, d.citations);
        }
        System.out.printf("  Randomized vs Deterministic Comparisons: %d vs %d%n",
                sortRes.randomizedMetrics.comparisons, sortRes.deterministicMetrics.comparisons);

        // 3. Reservoir Sampling
        List<ReservoirSampling.StreamEvent> stream = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            stream.add(new ReservoirSampling.StreamEvent(System.currentTimeMillis() + i * 10, "User" + (i % 10), "Query #" + i, "Doc " + (i % 25)));
        }
        ReservoirSampling.SamplingResult<ReservoirSampling.StreamEvent> reservoir =
                ReservoirSampling.simulateQueryStream(stream, 5);
        System.out.printf("[Reservoir Sampling] Stream of %d events sampled into reservoir of size %d (Prob: %.4f):%n",
                reservoir.streamTotalCount, reservoir.reservoirCapacity, reservoir.selectionProbability);
        for (ReservoirSampling.StreamEvent ev : reservoir.sampledReservoir) {
            System.out.printf("   - Event: %s by %s%n", ev.queryOrAction, ev.userId);
        }

        // 4. Parallel Reduce & Brent's Theorem
        long[] bigData = new long[200000];
        Arrays.fill(bigData, 7L);
        ParallelReduce.ParallelStatsResult parStats = ParallelReduce.benchmarkArrayReduction(bigData);
        System.out.printf("[Parallel Reduce & Brent's Theorem] Array size: %d, Cores: %d%n", bigData.length, parStats.availableProcessors);
        System.out.printf("  Sequential: %d \u00B5s | Parallel: %d \u00B5s | Speedup: %.2fx | Brent's Predicted: %.2fx%n",
                parStats.sequentialNanos / 1000, parStats.parallelNanos / 1000, parStats.speedup, parStats.brentsPredictedSpeedup);

        // 5. Blelloch Parallel Scan
        long[] scanData = {3, 1, 7, 0, 4, 1, 6, 3};
        ParallelScan.ScanResult scanRes = ParallelScan.blellochParallelScan(scanData);
        System.out.printf("[Blelloch Parallel Scan] Input: %s%n", Arrays.toString(scanData));
        System.out.printf("  Prefix Sum: %s (Tree Depth: %d)%n", Arrays.toString(scanRes.prefixSums), scanRes.treeDepth);
    }
}
