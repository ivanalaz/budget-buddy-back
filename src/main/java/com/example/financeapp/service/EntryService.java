package com.example.financeapp.service;

import com.example.financeapp.dto.CreateEntryRequestDto;
import com.example.financeapp.dto.EntryResponseDto;
import com.example.financeapp.dto.UpdateEntryRequestDto;
import com.example.financeapp.entity.Category;
import com.example.financeapp.entity.CurrencyCode;
import com.example.financeapp.entity.Entry;
import com.example.financeapp.entity.User;
import com.example.financeapp.exception.ResourceNotFoundException;
import com.example.financeapp.repository.CategoryRepository;
import com.example.financeapp.repository.EntryRepository;
import com.example.financeapp.repository.RecurringInstanceRepository;
import com.example.financeapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RecurringInstanceRepository recurringInstanceRepository;

    private Long getCurrentUserId() {
        return 1L;
    }

    /**
     * Returns entries for the current user within the specified date range, ordered by date descending (newest first).
     * Optional filters for category and currency are applied while preserving the date order.
     */
    public List<EntryResponseDto> getEntries(LocalDate from, LocalDate to, Optional<Long> categoryId, Optional<CurrencyCode> currency) {
        Long userId = getCurrentUserId();
        
        // Fetch entries ordered by date descending (newest first)
        List<Entry> entries = entryRepository.findByUserIdAndDateBetweenAndOptionalFilters(
                userId, from, to, categoryId.orElse(null), currency.orElse(null)
        );
        
        // Entries are already sorted by date DESC, id DESC from the repository query
        return entries.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public EntryResponseDto getEntry(Long id) {
        Long userId = getCurrentUserId();
        Entry entry = entryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found"));
        return mapToDto(entry);
    }

    @Transactional
    public EntryResponseDto createEntry(CreateEntryRequestDto dto) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Category not found");
        }

        if (category.isArchived()) {
            throw new IllegalArgumentException("Cannot create entry for archived category");
        }

        Entry entry = new Entry();
        entry.setUser(user);
        entry.setCategory(category);
        entry.setType(dto.getType());
        entry.setAmount(dto.getAmount());
        entry.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : CurrencyCode.RSD);
        entry.setDate(dto.getDate());
        entry.setNote(dto.getNote());

        Entry saved = entryRepository.save(entry);
        return mapToDto(saved);
    }

    @Transactional
    public EntryResponseDto updateEntry(Long id, UpdateEntryRequestDto dto) {
        Long userId = getCurrentUserId();
        Entry entry = entryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Category not found");
        }

        if (category.isArchived()) {
            throw new IllegalArgumentException("Cannot update entry to archived category");
        }

        entry.setCategory(category);
        entry.setType(dto.getType());
        entry.setAmount(dto.getAmount());
        entry.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : CurrencyCode.RSD);
        entry.setDate(dto.getDate());
        entry.setNote(dto.getNote());

        // Mark as manually overridden if this was a generated entry
        markAsManualOverrideIfGenerated(entry);

        Entry updated = entryRepository.save(entry);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteEntry(Long id) {
        Long userId = getCurrentUserId();
        Entry entry = entryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found"));

        // If this entry was generated from a recurring rule, delete the instance link first
        // to avoid foreign key constraint violation
        recurringInstanceRepository.findByTransactionId(id)
                .ifPresent(recurringInstanceRepository::delete);

        entryRepository.delete(entry);
    }

    private EntryResponseDto mapToDto(Entry entry) {
        EntryResponseDto.EntryResponseDtoBuilder builder = EntryResponseDto.builder()
                .id(entry.getId())
                .categoryId(entry.getCategory().getId())
                .categoryName(entry.getCategory().getName())
                .categoryEmoji(entry.getCategory().getEmoji())
                .type(entry.getType())
                .amount(entry.getAmount())
                .currency(entry.getCurrency())
                .date(entry.getDate())
                .note(entry.getNote());

        // Add recurring metadata if this entry was generated from a rule
        if (entry.getRecurringRule() != null) {
            builder.recurringRuleId(entry.getRecurringRule().getId())
                    .recurringRuleName(entry.getRecurringRule().getName())
                    .scheduledFor(entry.getScheduledFor())
                    .isGenerated(true);
        } else {
            builder.isGenerated(false);
        }

        return builder.build();
    }

    /**
     * Marks an entry as manually overridden if it was generated from a recurring rule.
     * This prevents bulk updates from the rule from overwriting user edits.
     */
    private void markAsManualOverrideIfGenerated(Entry entry) {
        if (entry.getRecurringRule() != null) {
            recurringInstanceRepository.findByTransactionId(entry.getId())
                    .ifPresent(instance -> {
                        instance.setIsManualOverride(true);
                        recurringInstanceRepository.save(instance);
                    });
        }
    }
}

