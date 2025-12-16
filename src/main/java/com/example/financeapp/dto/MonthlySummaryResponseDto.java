package com.example.financeapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryResponseDto {
    private String yearMonth;
    private List<CurrencyTotalDto> totalsByCurrency;
    private List<CategoryTotalDto> categoryTotals;
}

