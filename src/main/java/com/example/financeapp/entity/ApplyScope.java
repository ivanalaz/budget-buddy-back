package com.example.financeapp.entity;

/**
 * Defines how changes to a recurring rule should be applied to generated transactions.
 */
public enum ApplyScope {
    FUTURE_ONLY,    // Apply changes only to future generated transactions (default)
    ALL             // Apply changes to all generated transactions (past and future)
}
