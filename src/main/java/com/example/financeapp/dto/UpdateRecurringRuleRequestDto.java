package com.example.financeapp.dto;

import com.example.financeapp.entity.CurrencyCode;
import com.example.financeapp.entity.EndType;
import com.example.financeapp.entity.EntryType;
import com.example.financeapp.entity.RecurringKind;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecurringRuleRequestDto {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Kind is required")
    private RecurringKind kind;

    @NotNull(message = "Direction (INCOME/EXPENSE) is required")
    private EntryType direction;

    private Long categoryId;

    private CurrencyCode currency = CurrencyCode.RSD;

    @PositiveOrZero(message = "Amount must be zero or positive")
    private BigDecimal amountDefault;

    private Boolean amountIsVariable = false;

    @Min(value = 1, message = "Day of month must be between 1 and 31")
    @Max(value = 31, message = "Day of month must be between 1 and 31")
    private Integer dayOfMonth;

    private Boolean dateIsVariable = false;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End type is required")
    private EndType endType;

    @Min(value = 1, message = "Total occurrences must be at least 1")
    private Integer totalOccurrences;

    private String note;

    /**
     * Whether the rule is active. If false, no new transactions will be generated.
     */
    private Boolean isActive;
}
