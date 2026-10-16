package com.smartlibrary;

import com.smartlibrary.algorithms.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * SmartLibrary Platform REST API Server.
 * Built with Java's standard embedded HTTP server for zero external dependencies.
 * Provides RESTful JSON API endpoints for all algorithmic modules.
 */
public class Server {

    private static final int DEFAULT_PORT = 8080;

    // Sample Digital Library Repository
    public static class LibraryDocument {
        public int id;
        public String title;
        public String author;
        public int year;
        public int citations;
        public String category;
        public List<String> topics;
        public String abstractText;

        public LibraryDocument(int id, String title, String author, int year, int citations, String category, List<String> topics, String abstractText) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.year = year;
            this.citations = citations;
            this.category = category;
            this.topics = topics;
            this.abstractText = abstractText;
        }
    }

    private static final List<LibraryDocument> DOCUMENTS = new ArrayList<>();

    static {
        DOCUMENTS.add(new LibraryDocument(
                1,
                "Attention Is All You Need: Transformer Architectures",
                "Vaswani, Shazeer, Parmar, et al.",
                2017,
                112500,
                "Natural Language Processing",
                List.of("NLP", "Deep Learning", "Attention Mechanisms", "Transformers"),
                "The dominant sequence transduction models are based on complex recurrent or convolutional neural networks. We propose the Transformer, a model architecture eschewing recurrence and instead relying entirely on an attention mechanism to draw global dependencies between input and output. The Transformer allows for significantly more parallelization and can reach a new state of the art in translation quality after being trained for as little as twelve hours."
        ));

        DOCUMENTS.add(new LibraryDocument(
                2,
                "Deep Residual Learning for Image Recognition",
                "He, Zhang, Ren, Sun",
                2016,
                189000,
                "Computer Vision",
                List.of("Computer Vision", "Deep Learning", "Residual Networks"),
                "Deeper neural networks are more difficult to train. We present a residual learning framework to ease the training of networks that are substantially deeper than those used previously. We explicitly reformulate the layers as learning residual functions with reference to the layer inputs, instead of learning unreferenced functions. We evaluate these residual nets on the ImageNet dataset with a depth of up to 152 layers."
        ));

        DOCUMENTS.add(new LibraryDocument(
                3,
                "Fast Pattern Matching in Strings via Prefix Automation",
                "Knuth, Morris, Pratt",
                1977,
                42000,
                "Algorithms",
                List.of("String Algorithms", "Pattern Matching", "Automata Theory"),
                "An algorithm is presented which determines whether a given string of length m occurs as a substring in a given text string of length n. The algorithm requires at most m plus n character inspections in the worst case, and it needs only a small auxiliary memory of m integers."
        ));

        DOCUMENTS.add(new LibraryDocument(
                4,
                "Blelloch Work-Efficient Parallel Prefix Sums and Trees",
                "Guy E. Blelloch",
                1990,
                15400,
                "Parallel Computing",
                List.of("Parallel Algorithms", "Prefix Sums", "Blelloch Scan", "Brent's Theorem"),
                "Prefix sums are one of the most fundamental algorithmic primitives for parallel computing. We formulate a work-efficient parallel tree scan algorithm that requires O(N) operations and O(log N) step span depth across multi-processor architectures, establishing bounds consistent with Brent's scheduling theorem."
        ));

        DOCUMENTS.add(new LibraryDocument(
                5,
                "Network Flow Theory and Augmenting Path Optimization",
                "Edmonds, Karp, Dinic",
                1972,
                29800,
                "Graph Theory",
                List.of("Network Flow", "Max Flow", "Bipartite Matching", "Dinic Algorithm"),
                "This paper analyzes the computational complexity of the maximal network flow problem. We present theoretical refinements to the Ford-Fulkerson method by selecting shortest augmenting paths with breadth-first search and exploring Dinic's blocking flow layered level graphs."
        ));
    }

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            port = 8081;
            server = HttpServer.create(new InetSocketAddress(port), 0);
        }

        // Setup endpoints
        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/documents", new DocumentsHandler());
        server.createContext("/api/m1/search", new M1SearchHandler());
        server.createContext("/api/m2/suffix", new M2SuffixHandler());
        server.createContext("/api/m3/dp", new M3DPHandler());
        server.createContext("/api/m4/network-flow", new M4FlowHandler());
        server.createContext("/api/m5/approximation", new M5ApproxHandler());
        server.createContext("/api/m6/randomized-parallel", new M6RandomizedParallelHandler());
        server.createContext("/api/demo", new DemoHandler());

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("================================================================================");
        System.out.println(">>> SmartLibrary HTTP REST Server is ACTIVE on http://localhost:" + port + " <<<");
        System.out.println("================================================================================");
        System.out.println("Endpoints available:");
        System.out.println("  - GET  /api/health");
        System.out.println("  - GET  /api/documents");
        System.out.println("  - POST /api/m1/search");
        System.out.println("  - POST /api/m2/suffix");
        System.out.println("  - POST /api/m3/dp");
        System.out.println("  - POST /api/m4/network-flow");
        System.out.println("  - POST /api/m5/approximation");
        System.out.println("  - POST /api/m6/randomized-parallel");
        System.out.println("  - GET  /api/demo");
    }

    // CORS and JSON response utility
    public static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    public static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        // Basic key value parser
        String[] pairs = trimmed.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String val = kv[1].trim();
                if (val.startsWith("\"") && val.endsWith("\"")) {
                    val = val.substring(1, val.length() - 1);
                }
                map.put(key, val);
            }
        }
        return map;
    }

    private static int parseIntOrDefault(String value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(value.trim()); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    // Handlers
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = "{\"status\":\"UP\",\"platform\":\"SmartLibrary\",\"version\":\"1.0.0\",\"timestamp\":" + System.currentTimeMillis() + "}";
            sendJsonResponse(exchange, 200, json);
        }
    }

    static class DocumentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < DOCUMENTS.size(); i++) {
                LibraryDocument d = DOCUMENTS.get(i);
                sb.append(String.format(
                        "{\"id\":%d,\"title\":\"%s\",\"author\":\"%s\",\"year\":%d,\"citations\":%d,\"category\":\"%s\",\"topics\":%s,\"abstractText\":\"%s\"}",
                        d.id,
                        escapeJson(d.title),
                        escapeJson(d.author),
                        d.year,
                        d.citations,
                        escapeJson(d.category),
                        toJsonArray(d.topics),
                        escapeJson(d.abstractText)
                ));
                if (i < DOCUMENTS.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    static class M1SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = parseSimpleJson(body);

            String text = params.getOrDefault("text", DOCUMENTS.get(0).abstractText);
            String pattern = params.getOrDefault("pattern", "architecture");

            // Execute M1 algorithms
            KMP.KMPResult kmp = KMP.search(text, pattern);
            ZFunction.ZResult z = ZFunction.search(text, pattern);
            RabinKarp.RKResult rk = RabinKarp.search(text, pattern);

            List<String> keywords = Arrays.asList(pattern, "model", "network", "parallel", "learning");
            AhoCorasick ac = new AhoCorasick(keywords);
            AhoCorasick.ACResult acRes = ac.search(text);

            StringBuilder acMatches = new StringBuilder("[");
            for (int i = 0; i < acRes.matches.size(); i++) {
                AhoCorasick.Match m = acRes.matches.get(i);
                acMatches.append(String.format("{\"keyword\":\"%s\",\"start\":%d,\"end\":%d}", escapeJson(m.keyword), m.startIndex, m.endIndex));
                if (i < acRes.matches.size() - 1) acMatches.append(",");
            }
            acMatches.append("]");

            String json = String.format(
                    "{\"pattern\":\"%s\",\"textLength\":%d,\"kmp\":{\"matches\":%s,\"lps\":%s,\"comparisons\":%d,\"timeNanos\":%d},"
                            + "\"zFunction\":{\"matches\":%s,\"repeatedCount\":%d,\"timeNanos\":%d},"
                            + "\"rabinKarp\":{\"matches\":%s,\"spuriousHits\":%d,\"patternHash\":%d,\"timeNanos\":%d},"
                            + "\"ahoCorasick\":{\"matches\":%s,\"trieNodes\":%d,\"timeNanos\":%d}}",
                    escapeJson(pattern),
                    text.length(),
                    kmp.matchIndices.toString(),
                    Arrays.toString(kmp.lps),
                    kmp.comparisons,
                    kmp.executionTimeNanos,
                    z.matchIndices.toString(),
                    z.repeatedPhrases.size(),
                    z.executionTimeNanos,
                    rk.matchIndices.toString(),
                    rk.spuriousHits,
                    rk.patternHash,
                    rk.executionTimeNanos,
                    acMatches.toString(),
                    acRes.trieNodeCount,
                    acRes.executionTimeNanos
            );

            sendJsonResponse(exchange, 200, json);
        }
    }

    static class M2SuffixHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = parseSimpleJson(body);

            String text = params.getOrDefault("text", "banana_library_research_analysis_library_index$");
            String pattern = params.getOrDefault("pattern", "library");

            SuffixArray sa = new SuffixArray(text);
            SuffixArray.SuffixSearchResult sResult = sa.search(pattern);
            SuffixArray.RepeatedSubstringResult lcpRepeat = sa.findLongestRepeatedSubstring();

            SuffixAutomaton sam = new SuffixAutomaton(text);
            boolean samContains = sam.contains(pattern);
            int samFirstOcc = sam.findFirstOccurrence(pattern);
            long distinctSubstrings = sam.countDistinctSubstrings();

            String json = String.format(
                    "{\"text\":\"%s\",\"pattern\":\"%s\",\"suffixArray\":%s,\"lcp\":%s,\"search\":{\"found\":%b,\"occurrences\":%s,\"timeNanos\":%d},"
                            + "\"longestRepeatedSubstring\":{\"text\":\"%s\",\"length\":%d},"
                            + "\"suffixAutomaton\":{\"states\":%d,\"contains\":%b,\"firstOccurrence\":%d,\"distinctSubstrings\":%d}}",
                    escapeJson(text),
                    escapeJson(pattern),
                    Arrays.toString(sa.getSuffixArray()),
                    Arrays.toString(sa.getLcp()),
                    sResult.found,
                    sResult.occurrences.toString(),
                    sResult.executionTimeNanos,
                    escapeJson(lcpRepeat.substring),
                    lcpRepeat.length,
                    sam.getStateCount(),
                    samContains,
                    samFirstOcc,
                    distinctSubstrings
            );

            sendJsonResponse(exchange, 200, json);
        }
    }

    static class M3DPHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = parseSimpleJson(body);

            String query = params.getOrDefault("query", "algotithm");
            List<String> catalog = List.of("algorithm", "logarithm", "algebra", "automaton", "alignment", "transformer", "residual", "network");
            List<EditDistance.Suggestion> suggestions = EditDistance.suggest(query, catalog, 4);

            StringBuilder suggJson = new StringBuilder("[");
            for (int i = 0; i < suggestions.size(); i++) {
                EditDistance.Suggestion s = suggestions.get(i);
                suggJson.append(String.format("{\"candidate\":\"%s\",\"distance\":%d,\"similarity\":%.1f}",
                        escapeJson(s.candidate), s.distance, s.similarityPercentage));
                if (i < suggestions.size() - 1) suggJson.append(",");
            }
            suggJson.append("]");

            // Needleman-Wunsch Alignment
            String seq1 = params.getOrDefault("seq1", "DATASTRUCTURES");
            String seq2 = params.getOrDefault("seq2", "ALGORITHMICS");
            SequenceAlignment.AlignmentResult nw = SequenceAlignment.needlemanWunsch(seq1, seq2, 2, -1, -2);
            SequenceAlignment.AlignmentResult sw = SequenceAlignment.smithWaterman(seq1, seq2, 2, -1, -2);

            // Matrix Chain
            int[] dims = {10, 30, 5, 60, 20};
            String[] ops = {"DocTokenizer", "TermMatrix", "EmbeddingMap", "IndexJoin"};
            MatrixChainMultiplication.MCMResult mcm = MatrixChainMultiplication.solve(dims, ops);

            // Bitmask DP
            List<String> topics = List.of("NLP", "Graphs", "Cryptography", "Databases");
            List<BitmaskDP.ResourceBundle> bundles = List.of(
                    new BitmaskDP.ResourceBundle(1, "Bundle A (NLP & Graphs)", 50, List.of("NLP", "Graphs")),
                    new BitmaskDP.ResourceBundle(2, "Bundle B (Crypto & DB)", 70, List.of("Cryptography", "Databases")),
                    new BitmaskDP.ResourceBundle(3, "Bundle C (NLP & DB)", 90, List.of("NLP", "Databases")),
                    new BitmaskDP.ResourceBundle(4, "Bundle D (Security Suite)", 40, List.of("Cryptography"))
            );
            BitmaskDP.SelectionResult bitmaskRes = BitmaskDP.selectOptimalBundles(topics, bundles);

            StringBuilder bundlesJson = new StringBuilder("[");
            for (int i = 0; i < bitmaskRes.selectedBundles.size(); i++) {
                BitmaskDP.ResourceBundle b = bitmaskRes.selectedBundles.get(i);
                bundlesJson.append(String.format("{\"title\":\"%s\",\"cost\":%d,\"topics\":%s}",
                        escapeJson(b.title), b.cost, toJsonArray(b.coveredTopics)));
                if (i < bitmaskRes.selectedBundles.size() - 1) bundlesJson.append(",");
            }
            bundlesJson.append("]");

            String json = String.format(
                    "{\"queryCorrection\":{\"query\":\"%s\",\"suggestions\":%s},"
                            + "\"sequenceAlignment\":{\"global\":{\"score\":%d,\"seq1\":\"%s\",\"seq2\":\"%s\",\"markers\":\"%s\",\"identity\":%.1f},"
                            + "\"local\":{\"score\":%d,\"seq1\":\"%s\",\"seq2\":\"%s\",\"markers\":\"%s\",\"identity\":%.1f}},"
                            + "\"matrixChain\":{\"minCost\":%d,\"order\":\"%s\"},"
                            + "\"bitmaskSelection\":{\"minCost\":%d,\"topicsCovered\":%d,\"fullyCovered\":%b,\"selected\":%s}}",
                    escapeJson(query),
                    suggJson.toString(),
                    nw.score, escapeJson(nw.alignedSeq1), escapeJson(nw.alignedSeq2), escapeJson(nw.alignmentMarkers), nw.identityPercentage,
                    sw.score, escapeJson(sw.alignedSeq1), escapeJson(sw.alignedSeq2), escapeJson(sw.alignmentMarkers), sw.identityPercentage,
                    mcm.minCost, escapeJson(mcm.optimalOrder),
                    bitmaskRes.minCost, bitmaskRes.totalTopicsCovered, bitmaskRes.fullyCovered, bundlesJson.toString()
            );

            sendJsonResponse(exchange, 200, json);
        }
    }

    static class M4FlowHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            Map<String, String> params = parseSimpleJson(readRequestBody(exchange));
            int visit = parseIntOrDefault(params.get("visitDuration"), 30);
            int borrowed = parseIntOrDefault(params.get("booksBorrowed"), 3);
            int logins = parseIntOrDefault(params.get("appLogins"), 5);
            int c01 = 5 + Math.floorMod(visit, 17), c02 = 5 + Math.floorMod(borrowed * 3, 13);
            int c12 = 4 + Math.floorMod(logins, 9), c13 = 6 + Math.floorMod(visit + borrowed, 12);
            int c21 = 3 + Math.floorMod(logins + borrowed, 7), c24 = 5 + Math.floorMod(visit + logins, 15);
            int c32 = 4 + Math.floorMod(borrowed + 2, 8), c35 = 8 + Math.floorMod(visit, 18);
            int c43 = 4 + Math.floorMod(logins + 1, 8), c45 = 3 + Math.floorMod(borrowed + 1, 6);
            EdmondsKarp ek = new EdmondsKarp(6);
            ek.addEdge(0, 1, c01); ek.addEdge(0, 2, c02); ek.addEdge(1, 2, c12); ek.addEdge(1, 3, c13);
            ek.addEdge(2, 1, c21); ek.addEdge(2, 4, c24); ek.addEdge(3, 2, c32); ek.addEdge(3, 5, c35);
            ek.addEdge(4, 3, c43); ek.addEdge(4, 5, c45);
            EdmondsKarp.MaxFlowResult ekRes = ek.computeMaxFlow(0, 5);

            Dinic dinic = new Dinic(6);
            dinic.addEdge(0, 1, c01); dinic.addEdge(0, 2, c02); dinic.addEdge(1, 2, c12); dinic.addEdge(1, 3, c13);
            dinic.addEdge(2, 1, c21); dinic.addEdge(2, 4, c24); dinic.addEdge(3, 2, c32); dinic.addEdge(3, 5, c35);
            dinic.addEdge(4, 3, c43); dinic.addEdge(4, 5, c45);
            Dinic.DinicFlowResult dinicRes = dinic.computeMaxFlow(0, 5);

            // Bipartite Matching
            List<String> papers = List.of("Paper #101 (NLP Transformers)", "Paper #102 (Graph Neural Nets)", "Paper #103 (Quantum Crypto)");
            List<String> reviewers = List.of("Prof. Turing", "Dr. Shannon", "Dr. Lovelace");
            BipartiteMatching bm = new BipartiteMatching(papers, reviewers);
            bm.addEligibility(0, 0);
            bm.addEligibility(0, 1);
            bm.addEligibility(1, 1);
            bm.addEligibility(2, 2);
            BipartiteMatching.MatchingResult bmRes = bm.solve();

            StringBuilder assignJson = new StringBuilder("[");
            for (int i = 0; i < bmRes.assignments.size(); i++) {
                BipartiteMatching.Assignment a = bmRes.assignments.get(i);
                assignJson.append(String.format("{\"paper\":\"%s\",\"reviewer\":\"%s\"}", escapeJson(a.paper), escapeJson(a.reviewer)));
                if (i < bmRes.assignments.size() - 1) assignJson.append(",");
            }
            assignJson.append("]");

            String json = String.format(
                    "{\"edmondsKarp\":{\"maxFlow\":%d,\"bottlenecks\":%d,\"timeNanos\":%d},"
                            + "\"dinic\":{\"maxFlow\":%d,\"phases\":%d,\"timeNanos\":%d},"
                            + "\"bipartiteMatching\":{\"matchedCount\":%d,\"totalPapers\":%d,\"assignments\":%s,\"konigMinCover\":%s}}",
                    ekRes.maxFlow, ekRes.minCutBottlenecks.size(), ekRes.executionTimeNanos,
                    dinicRes.maxFlow, dinicRes.levelGraphPhases, dinicRes.executionTimeNanos,
                    bmRes.maxMatchingSize, papers.size(), assignJson.toString(), toJsonArray(bmRes.konigMinVertexCover)
            );

            sendJsonResponse(exchange, 200, json);
        }
    }

    static class M5ApproxHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            Map<String, String> params = parseSimpleJson(readRequestBody(exchange));
            int borrowed = parseIntOrDefault(params.get("booksBorrowed"), 3);
            int visit = parseIntOrDefault(params.get("visitDuration"), 30);
            int nodeCount = Math.max(4, Math.min(8, borrowed + 3));
            List<String> docTitles = new ArrayList<>();
            for (int i = 0; i < nodeCount; i++) docTitles.add("User Activity " + (i + 1));
            VertexCoverApproximation vca = new VertexCoverApproximation(docTitles.size(), docTitles);
            for (int i = 0; i < nodeCount - 1; i++) vca.addConflictEdge(i, i + 1);
            int extraA = Math.floorMod(visit, nodeCount);
            int extraB = (extraA + 2) % nodeCount;
            if (extraA != extraB) vca.addConflictEdge(extraA, extraB);

            VertexCoverApproximation.ComparisonResult comp = vca.compareAll();

            String json = String.format(
                    "{\"totalDocuments\":%d,\"totalConflictEdges\":%d,"
                            + "\"exact\":{\"size\":%d,\"vertices\":%s,\"ratio\":%.2f},"
                            + "\"approx2\":{\"size\":%d,\"vertices\":%s,\"ratio\":%.2f},"
                            + "\"greedy\":{\"size\":%d,\"vertices\":%s,\"ratio\":%.2f},"
                            + "\"karpReduction\":\"%s\"}",
                    comp.totalVertices, comp.totalEdges,
                    comp.exact.coverSize, toJsonArray(comp.exact.coverVertices), comp.exact.approximationRatio,
                    comp.approx2.coverSize, toJsonArray(comp.approx2.coverVertices), comp.approx2.approximationRatio,
                    comp.greedy.coverSize, toJsonArray(comp.greedy.coverVertices), comp.greedy.approximationRatio,
                    escapeJson(comp.reductionChainExplanation)
            );

            sendJsonResponse(exchange, 200, json);
        }
    }

    static class M6RandomizedParallelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            Map<String, String> params = parseSimpleJson(readRequestBody(exchange));
            // Miller-Rabin
            long testPrime = 1000000007L;
            MillerRabin.PrimalityResult mr = MillerRabin.isPrime(testPrime, 10);

            // Randomized Quicksort
            RandomizedQuickSort.Document[] docs = {
                    new RandomizedQuickSort.Document(1, "Deep Learning Foundations", "LeCun et al.", 2015, 45000, 9.8),
                    new RandomizedQuickSort.Document(2, "Attention Is All You Need", "Vaswani et al.", 2017, 85000, 9.9),
                    new RandomizedQuickSort.Document(3, "ImageNet Classification", "Krizhevsky et al.", 2012, 70000, 9.7),
                    new RandomizedQuickSort.Document(4, "Adam Optimizer", "Kingma & Ba", 2014, 60000, 9.5)
            };
            RandomizedQuickSort.BenchmarkResult sortRes = RandomizedQuickSort.runComparativeBenchmark(docs);

            // Reservoir Sampling
            List<ReservoirSampling.StreamEvent> stream = new ArrayList<>();
            for (int i = 1; i <= 60; i++) {
                stream.add(new ReservoirSampling.StreamEvent(System.currentTimeMillis() + i * 10, "Student" + (i % 8), "Search Query #" + i, "Paper " + (i % 15)));
            }
            ReservoirSampling.SamplingResult<ReservoirSampling.StreamEvent> reservoir = ReservoirSampling.simulateQueryStream(stream, 5);

            StringBuilder resvEvents = new StringBuilder("[");
            for (int i = 0; i < reservoir.sampledReservoir.size(); i++) {
                ReservoirSampling.StreamEvent ev = reservoir.sampledReservoir.get(i);
                resvEvents.append(String.format("{\"user\":\"%s\",\"action\":\"%s\",\"doc\":\"%s\"}",
                        escapeJson(ev.userId), escapeJson(ev.queryOrAction), escapeJson(ev.documentTitle)));
                if (i < reservoir.sampledReservoir.size() - 1) resvEvents.append(",");
            }
            resvEvents.append("]");

            // Parallel Reduce
            long[] bigData = new long[100000];
            Arrays.fill(bigData, 5L);
            ParallelReduce.ParallelStatsResult parStats = ParallelReduce.benchmarkArrayReduction(bigData);

            // Parallel Scan uses visit-duration values supplied from the CSV dataset.
            String rawScanValues = params.getOrDefault("scanValues", "3,1,7,0,4,1,6,3");
            String[] scanParts = rawScanValues.split(",");
            int scanLength = Math.max(1, Math.min(8, scanParts.length));
            long[] scanData = new long[scanLength];
            for (int i = 0; i < scanLength; i++) {
                try { scanData[i] = Math.max(0, Long.parseLong(scanParts[i].trim())); }
                catch (NumberFormatException ignored) { scanData[i] = 0; }
            }
            ParallelScan.ScanResult scanRes = ParallelScan.blellochParallelScan(scanData);

            String json = String.format(
                    "{\"millerRabin\":{\"number\":%d,\"isPrime\":%b,\"confidence\":%.6f,\"rounds\":%d},"
                            + "\"quickSort\":{\"docsCount\":%d,\"randComps\":%d,\"detComps\":%d},"
                            + "\"reservoirSampling\":{\"totalStream\":%d,\"capacity\":%d,\"samples\":%s},"
                            + "\"parallelReduce\":{\"cores\":%d,\"seqMicros\":%d,\"parMicros\":%d,\"speedup\":%.2f,\"brentsSpeedup\":%.2f},"
                            + "\"parallelScan\":{\"input\":%s,\"prefixSum\":%s,\"treeDepth\":%d}}",
                    mr.number, mr.isPrime, mr.confidencePercentage, mr.roundsTested,
                    docs.length, sortRes.randomizedMetrics.comparisons, sortRes.deterministicMetrics.comparisons,
                    reservoir.streamTotalCount, reservoir.reservoirCapacity, resvEvents.toString(),
                    parStats.availableProcessors, parStats.sequentialNanos / 1000, parStats.parallelNanos / 1000, parStats.speedup, parStats.brentsPredictedSpeedup,
                    Arrays.toString(scanData), Arrays.toString(scanRes.prefixSums), scanRes.treeDepth
            );

            sendJsonResponse(exchange, 200, json);
        }
    }

    static class DemoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
            PrintStream oldOut = System.out;
            try {
                System.setOut(ps);
                AlgorithmDemo.main(new String[0]);
            } finally {
                System.setOut(oldOut);
            }
            String output = baos.toString(StandardCharsets.UTF_8);
            String json = "{\"output\":\"" + escapeJson(output) + "\"}";
            sendJsonResponse(exchange, 200, json);
        }
    }

    // Helper utilities
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String toJsonArray(List<String> list) {
        if (list == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(escapeJson(list.get(i))).append("\"");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
