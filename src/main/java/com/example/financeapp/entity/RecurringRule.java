package com.example.financeapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Represents a recurring transaction rule.
 * Users define rules (salary, subscriptions, rent, loans, etc.) and the backend
 * automatically generates monthly transactions from those rules.
 */
@Entity
@Table(name = "recurring_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RecurringRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurringKind kind;

    /**
     * Direction indicates whether this rule generates income or expense transactions.
     * We reuse EntryType enum for consistency with existing transaction model.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType direction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode currency = CurrencyCode.RSD;

    /**
     * Default amount for generated transactions.
     * Can be null if amountIsVariable is true (user must fill in each month).
     */
    @Column(name = "amount_default", precision = 19, scale = 2)
    private BigDecimal amountDefault;

    /**
     * If true, amount varies each period (e.g., credit card bill, variable salary).
     * Generated transactions may use amountDefault as placeholder or require user input.
     */
    @Column(name = "amount_is_variable", nullable = false)
    private Boolean amountIsVariable = false;

    /**
     * Day of month for scheduled transactions (1-31).
     * If null, date is variable and user decides each month.
     */
    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    /**
     * If true, date varies each period (e.g., salary paid on different dates).
     * When true, dayOfMonth should be null or ignored.
     */
    @Column(name = "date_is_variable", nullable = false)
    private Boolean dateIsVariable = false;

    /**
     * The date when this recurring rule starts.
     * First transaction will be generated for this month.
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_type", nullable = false)
    private EndType endType;

    /**
     * For FIXED_TERM rules, the total number of occurrences (e.g., 12 installments).
     * Must be non-null when endType is FIXED_TERM.
     */
    @Column(name = "total_occurrences")
    private Integer totalOccurrences;

    @Column(columnDefinition = "TEXT")
    private String note;

    /**
     * If false, no new transactions will be generated for this rule.
     * Existing generated transactions remain unaffected.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
