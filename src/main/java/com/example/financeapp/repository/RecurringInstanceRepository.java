package com.example.financeapp.repository;

import com.example.financeapp.entity.RecurringInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringInstanceRepository extends JpaRepository<RecurringInstance, Long> {

    List<RecurringInstance> findByRuleId(Long ruleId);

    List<RecurringInstance> findByRuleIdOrderByScheduledForAsc(Long ruleId);

    Optional<RecurringInstance> findByTransactionId(Long transactionId);

    /**
     * Check if an instance already exists for a rule and scheduled date.
     * Used to prevent duplicate generation.
     */
    boolean existsByRuleIdAndScheduledFor(Long ruleId, LocalDate scheduledFor);

    /**
     * Check if any instance exists for a rule within a given month (year-month).
     * Used to prevent creating duplicate transactions when dayOfMonth changes.
     * This ensures only one transaction per rule per month, regardless of exact day.
     */
    @Query("SELECT COUNT(ri) > 0 FROM RecurringInstance ri WHERE ri.rule.id = :ruleId " +
           "AND YEAR(ri.scheduledFor) = :year AND MONTH(ri.scheduledFor) = :month")
    boolean existsByRuleIdAndYearMonth(
            @Param("ruleId") Long ruleId,
            @Param("year") int year,
            @Param("month") int month
    );

    /**
     * Find instance by rule and scheduled date.
     */
    Optional<RecurringInstance> findByRuleIdAndScheduledFor(Long ruleId, LocalDate scheduledFor);

    /**
     * Count how many instances have been created for a rule.
     * Used to track progress for FIXED_TERM rules.
     */
    long countByRuleId(Long ruleId);

    /**
     * Get the maximum occurrence index for a rule.
     * Used to determine the next index for FIXED_TERM rules.
     */
    @Query("SELECT MAX(ri.occurrenceIndex) FROM RecurringInstance ri WHERE ri.rule.id = :ruleId")
    Optional<Integer> findMaxOccurrenceIndexByRuleId(@Param("ruleId") Long ruleId);

    /**
     * Find all instances for a rule scheduled on or after a given date.
     * Used for applying rule updates to future instances.
     */
    @Query("SELECT ri FROM RecurringInstance ri WHERE ri.rule.id = :ruleId AND ri.scheduledFor >= :date")
    List<RecurringInstance> findByRuleIdAndScheduledForOnOrAfter(
            @Param("ruleId") Long ruleId,
            @Param("date") LocalDate date
    );

    /**
     * Find all instances for a rule.
     * Used for applying rule updates to all instances.
     */
    List<RecurringInstance> findByRuleIdAndIsManualOverrideFalse(Long ruleId);

    /**
     * Find future instances that haven't been manually edited.
     * Used for bulk updates when rule changes with FUTURE_ONLY scope.
     */
    @Query("SELECT ri FROM RecurringInstance ri WHERE ri.rule.id = :ruleId " +
           "AND ri.scheduledFor >= :date AND ri.isManualOverride = false")
    List<RecurringInstance> findFutureNonOverriddenInstances(
            @Param("ruleId") Long ruleId,
            @Param("date") LocalDate date
    );

    /**
     * Delete all instances for a rule where scheduled date is on or after given date.
     * Used when deactivating a rule with deleteFutureGenerated=true.
     */
    @Query("SELECT ri FROM RecurringInstance ri WHERE ri.rule.id = :ruleId " +
           "AND ri.scheduledFor >= :date AND ri.isManualOverride = false")
    List<RecurringInstance> findFutureInstancesForDeletion(
            @Param("ruleId") Long ruleId,
            @Param("date") LocalDate date
    );
}
