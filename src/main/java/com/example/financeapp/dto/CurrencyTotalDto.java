package com.example.financeapp.dto;

import com.example.financeapp.entity.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyTotalDto {
    private CurrencyCode currency;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal net;
}

