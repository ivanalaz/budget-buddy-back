package com.example.financeapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringInstanceResponseDto {

    private Long id;
    private Long ruleId;
    private String ruleName;
    private Long transactionId;
    private LocalDate scheduledFor;
    private Integer occurrenceIndex;
    private Boolean isManualOverride;
    private OffsetDateTime createdAt;
}
