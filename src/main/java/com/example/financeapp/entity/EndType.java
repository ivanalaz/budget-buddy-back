package com.example.financeapp.entity;

/**
 * Defines whether a recurring rule has a fixed number of occurrences or runs indefinitely.
 */
public enum EndType {
    FIXED_TERM,     // Has a defined number of occurrences (e.g., 12 loan installments)
    OPEN_ENDED      // Continues indefinitely until manually stopped
}
