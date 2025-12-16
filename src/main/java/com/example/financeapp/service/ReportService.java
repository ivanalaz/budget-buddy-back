package com.example.financeapp.service;

import com.example.financeapp.dto.CategoryTotalDto;
import com.example.financeapp.dto.CurrencyTotalDto;
import com.example.financeapp.dto.MonthlySummaryResponseDto;
import com.example.financeapp.entity.CurrencyCode;
import com.example.financeapp.entity.Entry;
import com.example.financeapp.entity.EntryType;
import com.example.financeapp.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final EntryRepository entryRepository;

    private Long getCurrentUserId() {
        return 1L;
    }

    public List<CategoryTotalDto> getSpendingByCategory(String yearMonth, CurrencyCode currency) {
        Long userId = getCurrentUserId();

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        List<Entry> entries = entryRepository.findByUserIdAndDateBetweenAndOptionalFilters(
                userId, from, to, null, currency
        );

        return buildSpendingByCategory(entries);
    }

    public MonthlySummaryResponseDto getMonthlySummary(String yearMonth) {
        Long userId = getCurrentUserId();
        
        // Parse yearMonth (e.g., "2025-11") to LocalDate range
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        List<Entry> entries = entryRepository.findByUserIdAndDateBetween(userId, from, to);

        // Group by currency and calculate totals
        Map<CurrencyCode, List<Entry>> entriesByCurrency = entries.stream()
                .collect(Collectors.groupingBy(Entry::getCurrency));

        List<CurrencyTotalDto> currencyTotals = new ArrayList<>();
        for (Map.Entry<CurrencyCode, List<Entry>> entry : entriesByCurrency.entrySet()) {
            CurrencyCode currency = entry.getKey();
            List<Entry> currencyEntries = entry.getValue();

            BigDecimal totalIncome = currencyEntries.stream()
                    .filter(e -> e.getType() == EntryType.INCOME)
                    .map(Entry::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalExpenses = currencyEntries.stream()
                    .filter(e -> e.getType() == EntryType.EXPENSE)
                    .map(Entry::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal net = totalIncome.subtract(totalExpenses);

            currencyTotals.add(CurrencyTotalDto.builder()
                    .currency(currency)
                    .totalIncome(totalIncome)
                    .totalExpenses(totalExpenses)
                    .net(net)
                    .build());
        }

        // Spending by category for RSD only (expenses only)
        List<Entry> rsdEntries = entriesByCurrency.getOrDefault(CurrencyCode.RSD, new ArrayList<>());
        List<CategoryTotalDto> categoryTotals = buildSpendingByCategory(rsdEntries);

        return MonthlySummaryResponseDto.builder()
                .yearMonth(yearMonth)
                .totalsByCurrency(currencyTotals)
                .categoryTotals(categoryTotals)
                .build();
    }

    private List<CategoryTotalDto> buildSpendingByCategory(List<Entry> entries) {
        // Only expenses
        List<Entry> expenseEntries = entries.stream()
                .filter(e -> e.getType() == EntryType.EXPENSE)
                .toList();

        Map<Long, List<Entry>> entriesByCategory = expenseEntries.stream()
                .collect(Collectors.groupingBy(e -> e.getCategory().getId()));

        // Precompute per-category totals (positive spending amounts)
        List<CategoryTotalDto> totals = new ArrayList<>();
        for (Map.Entry<Long, List<Entry>> entry : entriesByCategory.entrySet()) {
            Long categoryId = entry.getKey();
            List<Entry> categoryEntries = entry.getValue();

            String categoryName = categoryEntries.get(0).getCategory().getName();
            BigDecimal totalAmount = categoryEntries.stream()
                    .map(Entry::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totals.add(CategoryTotalDto.builder()
                    .categoryId(categoryId)
                    .categoryName(categoryName)
                    .totalAmount(totalAmount)
                    .percentage(BigDecimal.ZERO) // filled in below
                    .build());
        }

        BigDecimal totalSpending = totals.stream()
                .map(CategoryTotalDto::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSpending.compareTo(BigDecimal.ZERO) > 0) {
            List<CategoryTotalDto> withPercentages = new ArrayList<>();
            for (CategoryTotalDto t : totals) {
                BigDecimal pct = t.getTotalAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalSpending, 2, RoundingMode.HALF_UP);
                withPercentages.add(CategoryTotalDto.builder()
                        .categoryId(t.getCategoryId())
                        .categoryName(t.getCategoryName())
                        .totalAmount(t.getTotalAmount())
                        .percentage(pct)
                        .build());
            }
            totals = withPercentages;
        }

        return totals.stream()
                .sorted(Comparator.comparing(CategoryTotalDto::getTotalAmount).reversed())
                .toList();
    }
}

