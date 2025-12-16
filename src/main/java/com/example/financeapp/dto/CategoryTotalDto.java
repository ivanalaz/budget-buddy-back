package com.example.financeapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTotalDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal totalAmount;
    private BigDecimal percentage;
}

