package com.example.financeapp.repository;

import com.example.financeapp.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByUserIdAndIsArchivedFalse(Long userId);
    
    List<Category> findByUserId(Long userId);
    
    long countByUserId(Long userId);
}

