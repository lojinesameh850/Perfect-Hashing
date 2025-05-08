package com.example;

import java.util.Random;
import java.math.BigInteger;

/**
 * A universal hash function using polynomial hashing with good distribution.
 * Supports deterministic construction via seed or explicit parameters.
 */
public class UniversalHash {
    public final long a;   // Multiplier in universal hash — affects final distribution
    public final long b;   // Additive shift (offset/increment coefficient) — adds randomness
    private final long p;  // Prime modulus — controls arithmetic space, affects collisions
    private final long m;  // Hash table size — determines the range of the hash function
    private final int base; // Base for polynomial roll (e.g., like x in x^i) — randomized to reduce clustering

    /**
     * Constructs a universal hash function with a random seed.
     * @param m Hash table size
     * @param rand A fixed seed for deterministic behavior
     */
    public UniversalHash(long m, Random rand) {
        this.m = m;
        this.p = getRandomLargePrime(61, rand);
        this.a = (Math.abs(rand.nextLong()) % (p - 1)) + 1;
        this.b = Math.abs(rand.nextLong() % p);
        this.base = getRandomBase(rand); // Randomized base
    }    

    /**
     * Constructs a universal hash function with default randomness.
     * @param m Hash table size
     */
    public UniversalHash(long m) {
        this.m = m;
        Random rand = new Random();
        this.p = getRandomLargePrime(61, rand);
        this.a = (Math.abs(rand.nextLong()) % (p - 1)) + 1;
        this.b = Math.abs(rand.nextLong() % p);
        this.base = getRandomBase(rand); // Randomized base
    }    

    /**
     * Fully parameterized constructor for advanced control and testing.
     * @param m Hash table size
     * @param a Multiplier coefficient
     * @param b Increment coefficient
     * @param p Prime modulus
     */
    public UniversalHash(long m, long a, long b, long p) {
        this.m = m;
        this.a = a;
        this.b = b;
        this.p = p;
        this.base = 37; // Default fixed base for consistency — can be modified
    }

    public long hash(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        // Step 1 (Polynomial)
        // Converts the whole string into a single integer in [0, p-1]
        long hashValue = computePolynomialHash(key);
        // Step 2 (Multiply-Mod-Prime: h(x) = ((a * x + b) mod p) mod m)
        // Randomly re-maps the result to spread it evenly in [0, m)
        long universalHash = ((a * hashValue) % p + b) % p;
        // Final result in the range [0, m - 1]
        long finalHash = (long)(universalHash % m);
        return finalHash < 0 ? finalHash + m : finalHash;
    }

    // Polynomial hash function is h(x) = ((a * x + b) mod p) mod m
    private long computePolynomialHash(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        // Return a small prime if string is empty, avoids zero hash
        if (key.isEmpty()) {
            return 17;
        }
        long hash = 0;
        // Build the polynomial hash (key[0]*base^0 + key[1]*base^1 + key[2]*base^2 + ...) mod p
        for (int i = 0; i < key.length(); i++) {
            // Multiply and mod at every step to avoid overflow
            hash = (hash * base + key.charAt(i)) % p;
            // Ensure hash stays non-negative
            if (hash < 0) {
                hash += p;
            }
        }
        return hash;
    }    

    public static long getRandomLargePrime(int bitLength, Random rand) {
        BigInteger prime = BigInteger.probablePrime(bitLength, rand);
        return prime.longValue();
    }

    private static int getRandomBase(Random rand) {
        // Pick base in [31, 127]
        int[] goodBases = {31, 33, 37, 39, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127};
        return goodBases[rand.nextInt(goodBases.length)];
    }

    @Override
    public String toString() {
        return String.format("UniversalHash(m=%d, a=%d, b=%d, p=%d, base=%d)", m, a, b, p, base);
    }
}
