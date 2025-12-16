package com.example.financeapp.controller;

import com.example.financeapp.dto.CategoryTotalDto;
import com.example.financeapp.dto.MonthlySummaryResponseDto;
import com.example.financeapp.entity.CurrencyCode;
import com.example.financeapp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/months")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/{yearMonth}/summary")
    public ResponseEntity<MonthlySummaryResponseDto> getMonthlySummary(@PathVariable String yearMonth) {
        MonthlySummaryResponseDto summary = reportService.getMonthlySummary(yearMonth);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{yearMonth}/spending-by-category")
    public ResponseEntity<List<CategoryTotalDto>> getSpendingByCategory(
            @PathVariable String yearMonth,
            @RequestParam(name = "currency", defaultValue = "RSD") CurrencyCode currency
    ) {
        return ResponseEntity.ok(reportService.getSpendingByCategory(yearMonth, currency));
    }
}

