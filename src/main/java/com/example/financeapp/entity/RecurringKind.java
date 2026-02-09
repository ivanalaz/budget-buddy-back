package com.example.financeapp.entity;

/**
 * Defines the type/category of a recurring rule.
 */
public enum RecurringKind {
    LOAN,           // Car loan, mortgage, personal loan
    SUBSCRIPTION,   // Netflix, Spotify, gym membership
    INCOME,         // Salary, freelance income
    BILL,           // Rent, utilities, insurance
    CREDIT_CARD,    // Credit card monthly payment
    OTHER           // Any other recurring transaction
}
