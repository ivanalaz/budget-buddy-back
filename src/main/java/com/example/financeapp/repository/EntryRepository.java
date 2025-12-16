package com.example.financeapp.repository;

import com.example.financeapp.entity.CurrencyCode;
import com.example.financeapp.entity.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {
    
    List<Entry> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);
    
    // Returns entries ordered by date descending (newest first), with id descending as tie-breaker
    @Query("SELECT e FROM Entry e WHERE e.user.id = :userId AND e.date BETWEEN :from AND :to " +
           "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
           "AND (:currency IS NULL OR e.currency = :currency) " +
           "ORDER BY e.date DESC, e.id DESC")
    List<Entry> findByUserIdAndDateBetweenAndOptionalFilters(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("categoryId") Long categoryId,
            @Param("currency") CurrencyCode currency
    );
    
    // Returns entries ordered by date descending (newest first), with id descending as tie-breaker
    List<Entry> findByUserIdAndDateBetweenOrderByDateDescIdDesc(Long userId, LocalDate from, LocalDate to);
    
    List<Entry> findByUserId(Long userId);
    
    Optional<Entry> findByIdAndUserId(Long id, Long userId);
}

