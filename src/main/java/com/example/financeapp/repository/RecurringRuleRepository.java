package com.example.financeapp.repository;

import com.example.financeapp.entity.RecurringRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringRuleRepository extends JpaRepository<RecurringRule, Long> {

    List<RecurringRule> findByUserId(Long userId);

    List<RecurringRule> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<RecurringRule> findByIdAndUserId(Long id, Long userId);

    List<RecurringRule> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find all active rules for a user that should generate transactions.
     * This includes rules that haven't reached their occurrence limit for FIXED_TERM,
     * or any active OPEN_ENDED rule.
     */
    @Query("SELECT r FROM RecurringRule r WHERE r.user.id = :userId AND r.isActive = true")
    List<RecurringRule> findActiveRulesForGeneration(@Param("userId") Long userId);

    /**
     * Count active rules for a user.
     */
    long countByUserIdAndIsActiveTrue(Long userId);
}
