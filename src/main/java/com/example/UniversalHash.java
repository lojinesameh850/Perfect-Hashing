package com.example;

import java.util.Random;
import java.math.BigInteger;

public class UniversalHash {
    public final long a;
    public final long b;
    private final long p;
    private final long m;
    private final int base;

    /**
     * Constructor with user-defined randomness.
     */
    public UniversalHash(long m, Random rand) {
        this.m = m;
        this.p = getRandomLargePrime(61, rand);
        this.a = 1 + Math.abs(rand.nextLong() % (p - 1));  // Ensure a ∈ [1, p-1]
        this.b = Math.abs(rand.nextLong() % p);            // b ∈ [0, p-1]
        this.base = getRandomBase(rand);
    }

    /**
     * Constructor with default random.
     */
    public UniversalHash(long m) {
        this(m, new Random());
    }

    /**
     * Manual construction — useful for testing or deterministic settings.
     */
    public UniversalHash(long m, long a, long b, long p) {
        this.m = m;
        this.a = a;
        this.b = b;
        this.p = p;
        this.base = 37; // Safe fixed base
    }

    /**
     * Main hash method — universal hash with polynomial string hash.
     */
    public long hash(String key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");

        long polyHash = computePolynomialHash(key); // Step 1
        long universalHash = ((a * polyHash + b) % p); // Step 2
        long finalHash = universalHash % m;            // Step 3

        return finalHash < 0 ? finalHash + m : finalHash;
    }

    /**
     * Efficient polynomial string hash mod p.
     */
    private long computePolynomialHash(String key) {
        if (key.isEmpty()) return 17; // Small prime fallback for empty strings

        long hash = 0;
        for (int i = 0; i < key.length(); i++) {
            hash = (hash * base + key.charAt(i)) % p;
        }
        return hash;
    }

    /**
     * Generate a large random prime with specified bit length.
     */
    public static long getRandomLargePrime(int bitLength, Random rand) {
        return BigInteger.probablePrime(bitLength, rand).longValue();
    }

    /**
     * Choose a good polynomial base from known primes.
     */
    private static int getRandomBase(Random rand) {
        int[] bases = {31, 33, 37, 39, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127};
        return bases[rand.nextInt(bases.length)];
    }

    @Override
    public String toString() {
        return String.format("UniversalHash(m=%d, a=%d, b=%d, p=%d, base=%d)", m, a, b, p, base);
    }
}
