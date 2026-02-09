package com.example.financeapp.dto;

import com.example.financeapp.entity.CurrencyCode;
import com.example.financeapp.entity.EndType;
import com.example.financeapp.entity.EntryType;
import com.example.financeapp.entity.RecurringKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringRuleResponseDto {

    private Long id;
    private String name;
    private RecurringKind kind;
    private EntryType direction;
    
    private Long categoryId;
    private String categoryName;
    private String categoryEmoji;
    
    private CurrencyCode currency;
    private BigDecimal amountDefault;
    private Boolean amountIsVariable;
    
    private Integer dayOfMonth;
    private Boolean dateIsVariable;
    
    private LocalDate startDate;
    private EndType endType;
    private Integer totalOccurrences;
    
    private String note;
    private Boolean isActive;
    
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Computed fields
    
    /**
     * The next scheduled date for this rule.
     * Null if the rule is inactive, ended, or has variable dates.
     */
    private LocalDate nextScheduledDate;

    /**
     * Number of instances/transactions created so far.
     */
    private Integer createdCount;

    /**
     * Progress info for FIXED_TERM rules.
     * Format: "5/12" meaning 5 out of 12 occurrences.
     * Null for OPEN_ENDED rules.
     */
    private String progress;

    /**
     * Percentage progress for FIXED_TERM rules (0-100).
     * Null for OPEN_ENDED rules.
     */
    private Integer progressPercent;
}
