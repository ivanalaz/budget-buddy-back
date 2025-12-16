package com.example.financeapp.dto;

import com.example.financeapp.entity.CurrencyCode;
import com.example.financeapp.entity.EntryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEntryRequestDto {
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    @NotNull(message = "Entry type is required")
    private EntryType type;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private CurrencyCode currency = CurrencyCode.RSD;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    private String note;
}

