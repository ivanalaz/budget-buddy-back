package com.example.financeapp.service;

import com.example.financeapp.dto.CategoryResponseDto;
import com.example.financeapp.dto.CreateCategoryRequestDto;
import com.example.financeapp.dto.UpdateCategoryRequestDto;
import com.example.financeapp.entity.Category;
import com.example.financeapp.entity.User;
import com.example.financeapp.exception.ResourceNotFoundException;
import com.example.financeapp.repository.CategoryRepository;
import com.example.financeapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        return 1L;
    }

    public List<CategoryResponseDto> getAllActiveCategoriesForCurrentUser() {
        Long userId = getCurrentUserId();
        List<Category> categories = categoryRepository.findByUserIdAndIsArchivedFalse(userId);
        return categories.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponseDto createCategory(CreateCategoryRequestDto dto) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = new Category();
        category.setUser(user);
        category.setName(dto.getName());
        category.setEmoji(dto.getEmoji());
        category.setDefault(false);
        category.setArchived(false);

        Category saved = categoryRepository.save(category);
        return mapToDto(saved);
    }

    @Transactional
    public CategoryResponseDto updateCategory(Long id, UpdateCategoryRequestDto dto) {
        Long userId = getCurrentUserId();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Category not found");
        }

        category.setName(dto.getName());
        category.setEmoji(dto.getEmoji());

        Category updated = categoryRepository.save(category);
        return mapToDto(updated);
    }

    @Transactional
    public void archiveCategory(Long id) {
        Long userId = getCurrentUserId();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Category not found");
        }

        category.setArchived(true);
        categoryRepository.save(category);
    }

    private CategoryResponseDto mapToDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .emoji(category.getEmoji())
                .isDefault(category.isDefault())
                .isArchived(category.isArchived())
                .build();
    }
}

