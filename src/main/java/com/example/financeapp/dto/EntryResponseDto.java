package com.example.financeapp.dto;

import com.example.financeapp.entity.CurrencyCode;
import com.example.financeapp.entity.EntryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryResponseDto {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryEmoji;
    private EntryType type;
    private BigDecimal amount;
    private CurrencyCode currency;
    private LocalDate date;
    private String note;

    // Recurring transaction metadata
    
    /**
     * ID of the recurring rule that generated this entry.
     * Null for manually created entries.
     */
    private Long recurringRuleId;

    /**
     * Name of the recurring rule (for display purposes).
     * Null for manually created entries.
     */
    private String recurringRuleName;

    /**
     * The originally scheduled date if this entry was auto-generated.
     * Null for manually created entries.
     */
    private LocalDate scheduledFor;

    /**
     * True if this entry was auto-generated from a recurring rule.
     */
    private Boolean isGenerated;
}

