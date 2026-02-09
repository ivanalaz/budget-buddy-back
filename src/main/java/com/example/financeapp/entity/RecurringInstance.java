package com.example.financeapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Links a generated transaction (Entry) to the recurring rule that created it.
 * This allows tracking which transactions were auto-generated and their original schedule.
 */
@Entity
@Table(name = "recurring_instances", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"transaction_id"}),
    @UniqueConstraint(columnNames = {"rule_id", "scheduled_for"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RecurringInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private RecurringRule rule;

    /**
     * The transaction that was generated from this rule.
     * One-to-one relationship: each instance links to exactly one transaction.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Entry transaction;

    /**
     * The originally scheduled date for this transaction.
     * Useful for auditing and detecting if the user changed the actual date.
     */
    @Column(name = "scheduled_for", nullable = false)
    private LocalDate scheduledFor;

    /**
     * For FIXED_TERM rules, the occurrence index (1, 2, 3... up to totalOccurrences).
     * Null for OPEN_ENDED rules.
     */
    @Column(name = "occurrence_index")
    private Integer occurrenceIndex;

    /**
     * Indicates if the user has manually edited this transaction after generation.
     * When true, bulk updates from rule changes may skip this transaction.
     */
    @Column(name = "is_manual_override", nullable = false)
    private Boolean isManualOverride = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
