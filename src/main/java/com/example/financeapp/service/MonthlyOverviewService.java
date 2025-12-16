package com.example.financeapp.service;

import com.example.financeapp.dto.MonthlyOverviewResponseDto;
import com.example.financeapp.dto.UpsertMonthlyOverviewRequestDto;
import com.example.financeapp.entity.MonthlyOverview;
import com.example.financeapp.entity.User;
import com.example.financeapp.exception.ResourceNotFoundException;
import com.example.financeapp.repository.MonthlyOverviewRepository;
import com.example.financeapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MonthlyOverviewService {

    private final MonthlyOverviewRepository monthlyOverviewRepository;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        return 1L;
    }

    public MonthlyOverviewResponseDto getOverview(String yearMonth) {
        Long userId = getCurrentUserId();
        return monthlyOverviewRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .map(this::mapToDto)
                .orElse(MonthlyOverviewResponseDto.builder()
                        .yearMonth(yearMonth)
                        .startingTotal(null)
                        .note(null)
                        .build());
    }

    @Transactional
    public MonthlyOverviewResponseDto upsertOverview(String yearMonth, UpsertMonthlyOverviewRequestDto dto) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MonthlyOverview overview = monthlyOverviewRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .orElse(new MonthlyOverview());

        overview.setUser(user);
        overview.setYearMonth(yearMonth);
        overview.setStartingTotal(dto.getStartingTotal());
        overview.setNote(dto.getNote());

        MonthlyOverview saved = monthlyOverviewRepository.save(overview);
        return mapToDto(saved);
    }

    private MonthlyOverviewResponseDto mapToDto(MonthlyOverview overview) {
        return MonthlyOverviewResponseDto.builder()
                .yearMonth(overview.getYearMonth())
                .startingTotal(overview.getStartingTotal())
                .note(overview.getNote())
                .build();
    }
}

