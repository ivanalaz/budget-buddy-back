package com.example.financeapp.repository;

import com.example.financeapp.entity.MonthlyOverview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonthlyOverviewRepository extends JpaRepository<MonthlyOverview, Long> {
    
    Optional<MonthlyOverview> findByUserIdAndYearMonth(Long userId, String yearMonth);
}

