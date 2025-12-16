package com.example.financeapp;

import com.example.financeapp.entity.Category;
import com.example.financeapp.entity.User;
import com.example.financeapp.repository.CategoryRepository;
import com.example.financeapp.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data initializer that ensures default user and categories for local development.
 * This initializer is idempotent - it will only create data if it doesn't already exist.
 */
@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data initialization...");

        // 1) Ensure user with id = 1L exists
        Optional<User> existingUser = userRepository.findById(1L);
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("User with id=1 already exists: {}", user.getEmail());
        } else {
            // Use native query to insert user with id=1, bypassing JPA's ID generation
            entityManager.createNativeQuery(
                "INSERT INTO users (id, email, name) VALUES (1, 'demo@budgetbuddy.local', 'Demo User') " +
                "ON CONFLICT (id) DO NOTHING"
            ).executeUpdate();
            
            // Refresh to get the user entity
            user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Failed to create user with id=1"));
            log.info("Created default user with id=1: {}", user.getEmail());
        }

        // 2) Seed default categories only if this user has no categories yet
        long categoryCount = categoryRepository.countByUserId(user.getId());
        if (categoryCount == 0) {
            log.info("No categories found for user id={}, creating default categories...", user.getId());
            
            List<Category> defaultCategories = new ArrayList<>();
            
            defaultCategories.add(createCategory(user, "Food", "🍽️"));
            defaultCategories.add(createCategory(user, "Groceries", "🛒"));
            defaultCategories.add(createCategory(user, "Dog", "🐶"));
            defaultCategories.add(createCategory(user, "Rent", "🏠"));
            defaultCategories.add(createCategory(user, "Transport", "🚌"));
            defaultCategories.add(createCategory(user, "Fun", "🎉"));
            defaultCategories.add(createCategory(user, "Health", "🩺"));
            defaultCategories.add(createCategory(user, "Subscriptions", "📺"));
            defaultCategories.add(createCategory(user, "Gifts", "🎁"));
            defaultCategories.add(createCategory(user, "Other", "✨"));
            
            categoryRepository.saveAll(defaultCategories);
            log.info("Created {} default categories for user id={}", defaultCategories.size(), user.getId());
        } else {
            log.info("User id={} already has {} categories, skipping category creation", user.getId(), categoryCount);
        }

        log.info("Data initialization completed.");
    }

    private Category createCategory(User user, String name, String emoji) {
        Category category = new Category();
        category.setUser(user);
        category.setName(name);
        category.setEmoji(emoji);
        category.setDefault(true);
        category.setArchived(false);
        return category;
    }
}

