package com.example.financeapp.controller;

import com.example.financeapp.dto.MonthlyOverviewResponseDto;
import com.example.financeapp.dto.UpsertMonthlyOverviewRequestDto;
import com.example.financeapp.service.MonthlyOverviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/months")
@RequiredArgsConstructor
public class MonthlyOverviewController {

    private final MonthlyOverviewService monthlyOverviewService;

    @GetMapping("/{yearMonth}/overview")
    public ResponseEntity<MonthlyOverviewResponseDto> getOverview(@PathVariable String yearMonth) {
        MonthlyOverviewResponseDto overview = monthlyOverviewService.getOverview(yearMonth);
        return ResponseEntity.ok(overview);
    }

    @PutMapping("/{yearMonth}/overview")
    public ResponseEntity<MonthlyOverviewResponseDto> upsertOverview(
            @PathVariable String yearMonth,
            @Valid @RequestBody UpsertMonthlyOverviewRequestDto dto) {
        MonthlyOverviewResponseDto overview = monthlyOverviewService.upsertOverview(yearMonth, dto);
        return ResponseEntity.ok(overview);
    }
}

