package com.smartlibrary.algorithms;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Module 6 (CO6): Miller-Rabin Randomized Primality Test.
 * Applied in SmartLibrary for cryptographic document signatures, hash-table sizing, and ISBN authentication keys.
 * Time Complexity: O(k * log^3 N), Error Probability: <= 4^(-k).
 */
public class MillerRabin {

    public static class PrimalityResult {
        public long number;
        public boolean isPrime;
        public int roundsTested;
        public double confidencePercentage;
        public List<String> steps;
        public long executionTimeNanos;

        public PrimalityResult(long number, boolean isPrime, int roundsTested, double confidencePercentage, List<String> steps, long executionTimeNanos) {
            this.number = number;
            this.isPrime = isPrime;
            this.roundsTested = roundsTested;
            this.confidencePercentage = confidencePercentage;
            this.steps = steps;
            this.executionTimeNanos = executionTimeNanos;
        }
    }

    private static final Random random = new Random();

    /**
     * Modular multiplication (a * b) % mod avoiding 64-bit overflow.
     */
    public static long mulMod(long a, long b, long mod) {
        return BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)).mod(BigInteger.valueOf(mod)).longValue();
    }

    /**
     * Modular exponentiation (base^exp) % mod.
     */
    public static long powerMod(long base, long exp, long mod) {
        long res = 1;
        base = base % mod;
        while (exp > 0) {
            if ((exp & 1) == 1) res = mulMod(res, base, mod);
            base = mulMod(base, base, mod);
            exp >>= 1;
        }
        return res;
    }

    /**
     * Tests primality using Miller-Rabin with k rounds.
     */
    public static PrimalityResult isPrime(long n, int k) {
        long startTime = System.nanoTime();
        List<String> steps = new ArrayList<>();

        if (n <= 1) {
            return new PrimalityResult(n, false, 0, 100.0, List.of("n <= 1 is composite"), System.nanoTime() - startTime);
        }
        if (n == 2 || n == 3) {
            return new PrimalityResult(n, true, 0, 100.0, List.of("2 and 3 are prime numbers"), System.nanoTime() - startTime);
        }
        if (n % 2 == 0) {
            return new PrimalityResult(n, false, 0, 100.0, List.of("Even number > 2 is composite"), System.nanoTime() - startTime);
        }

        // Write n - 1 as 2^s * d
        long d = n - 1;
        int s = 0;
        while (d % 2 == 0) {
            d /= 2;
            s++;
        }

        steps.add(String.format("Decomposed n-1 = %d as 2^%d * %d", n - 1, s, d));

        // Deterministic bases for 64-bit integers: 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37
        long[] bases = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37};
        int rounds = Math.min(k, bases.length);

        for (int r = 0; r < rounds; r++) {
            long a = bases[r];
            if (n <= a) break;

            long x = powerMod(a, d, n);
            steps.add(String.format("Round %d: Witness base a=%d, a^d mod n = %d", r + 1, a, x));

            if (x == 1 || x == n - 1) {
                continue;
            }

            boolean composite = true;
            for (int i = 0; i < s - 1; i++) {
                x = mulMod(x, x, n);
                steps.add(String.format("  Squaring step %d: x = %d", i + 1, x));
                if (x == n - 1) {
                    composite = false;
                    break;
                }
            }

            if (composite) {
                steps.add(String.format("Witness a=%d proved %d is COMPOSITE", a, n));
                long elapsed = System.nanoTime() - startTime;
                return new PrimalityResult(n, false, r + 1, 100.0, steps, elapsed);
            }
        }

        double confidence = 100.0 * (1.0 - Math.pow(0.25, rounds));
        steps.add(String.format("Passed all %d rounds. Number %d is PRIME (confidence: %.6f%%)", rounds, n, confidence));

        long elapsed = System.nanoTime() - startTime;
        return new PrimalityResult(n, true, rounds, confidence, steps, elapsed);
    }

    /**
     * Generates a random probable prime of specified bit length.
     */
    public static long generateProbablePrime(int bits) {
        while (true) {
            long val = (1L << (bits - 1)) | (random.nextLong() & ((1L << bits) - 1)) | 1L;
            if (val > 0 && isPrime(val, 20).isPrime) {
                return val;
            }
        }
    }
}
