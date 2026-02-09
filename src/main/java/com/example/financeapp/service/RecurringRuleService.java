package com.example.financeapp.service;

import com.example.financeapp.dto.*;
import com.example.financeapp.entity.*;
import com.example.financeapp.exception.ResourceNotFoundException;
import com.example.financeapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringRuleService {

    private final RecurringRuleRepository ruleRepository;
    private final RecurringInstanceRepository instanceRepository;
    private final EntryRepository entryRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // No longer generating future transactions - sync only creates transactions for dates <= today

    private Long getCurrentUserId() {
        return 1L;
    }

    // ==================== CRUD Operations ====================

    public List<RecurringRuleResponseDto> getAllRules() {
        Long userId = getCurrentUserId();
        List<RecurringRule> rules = ruleRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return rules.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public RecurringRuleResponseDto getRule(Long id) {
        Long userId = getCurrentUserId();
        RecurringRule rule = ruleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring rule not found"));
        return mapToDto(rule);
    }

    @Transactional
    public RecurringRuleResponseDto createRule(CreateRecurringRuleRequestDto dto) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateRuleDto(dto.getEndType(), dto.getTotalOccurrences(),
                dto.getAmountIsVariable(), dto.getAmountDefault(),
                dto.getDateIsVariable(), dto.getDayOfMonth());

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            if (!category.getUser().getId().equals(userId)) {
                throw new ResourceNotFoundException("Category not found");
            }
        }

        RecurringRule rule = new RecurringRule();
        rule.setUser(user);
        rule.setName(dto.getName());
        rule.setKind(dto.getKind());
        rule.setDirection(dto.getDirection());
        rule.setCategory(category);
        rule.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : CurrencyCode.RSD);
        rule.setAmountDefault(dto.getAmountDefault());
        rule.setAmountIsVariable(dto.getAmountIsVariable() != null ? dto.getAmountIsVariable() : false);
        rule.setDayOfMonth(dto.getDayOfMonth());
        rule.setDateIsVariable(dto.getDateIsVariable() != null ? dto.getDateIsVariable() : false);
        rule.setStartDate(dto.getStartDate());
        rule.setEndType(dto.getEndType());
        rule.setTotalOccurrences(dto.getTotalOccurrences());
        rule.setNote(dto.getNote());
        rule.setIsActive(true);

        RecurringRule saved = ruleRepository.save(rule);
        log.info("Created recurring rule: {} (id={})", saved.getName(), saved.getId());
        return mapToDto(saved);
    }

    @Transactional
    public RecurringRuleResponseDto updateRule(Long id, UpdateRecurringRuleRequestDto dto, ApplyScope applyScope) {
        Long userId = getCurrentUserId();
        RecurringRule rule = ruleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring rule not found"));

        validateRuleDto(dto.getEndType(), dto.getTotalOccurrences(),
                dto.getAmountIsVariable(), dto.getAmountDefault(),
                dto.getDateIsVariable(), dto.getDayOfMonth());

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            if (!category.getUser().getId().equals(userId)) {
                throw new ResourceNotFoundException("Category not found");
            }
        }

        // Update rule fields
        rule.setName(dto.getName());
        rule.setKind(dto.getKind());
        rule.setDirection(dto.getDirection());
        rule.setCategory(category);
        rule.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : CurrencyCode.RSD);
        rule.setAmountDefault(dto.getAmountDefault());
        rule.setAmountIsVariable(dto.getAmountIsVariable() != null ? dto.getAmountIsVariable() : false);
        rule.setDayOfMonth(dto.getDayOfMonth());
        rule.setDateIsVariable(dto.getDateIsVariable() != null ? dto.getDateIsVariable() : false);
        rule.setStartDate(dto.getStartDate());
        rule.setEndType(dto.getEndType());
        rule.setTotalOccurrences(dto.getTotalOccurrences());
        rule.setNote(dto.getNote());
        if (dto.getIsActive() != null) {
            rule.setIsActive(dto.getIsActive());
        }

        RecurringRule saved = ruleRepository.save(rule);

        // Apply changes to generated transactions based on scope
        applyRuleChangesToInstances(saved, applyScope);

        log.info("Updated recurring rule: {} (id={}, scope={})", saved.getName(), saved.getId(), applyScope);
        return mapToDto(saved);
    }

    @Transactional
    public RecurringRuleResponseDto toggleActive(Long id, ToggleActiveRequestDto dto) {
        Long userId = getCurrentUserId();
        RecurringRule rule = ruleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring rule not found"));

        boolean wasActive = rule.getIsActive();
        rule.setIsActive(dto.getIsActive());

        // If deactivating and deleteFutureGenerated is true, delete future unedited instances
        if (wasActive && !dto.getIsActive() && Boolean.TRUE.equals(dto.getDeleteFutureGenerated())) {
            deleteFutureInstances(rule.getId());
        }

        RecurringRule saved = ruleRepository.save(rule);
        log.info("Toggled recurring rule active status: {} (id={}, isActive={})",
                saved.getName(), saved.getId(), saved.getIsActive());
        return mapToDto(saved);
    }

    @Transactional
    public void deleteRule(Long id, boolean deleteFutureGenerated) {
        Long userId = getCurrentUserId();
        RecurringRule rule = ruleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring rule not found"));

        // Soft delete: just deactivate
        rule.setIsActive(false);

        if (deleteFutureGenerated) {
            deleteFutureInstances(rule.getId());
        }

        ruleRepository.save(rule);
        log.info("Soft-deleted recurring rule: {} (id={})", rule.getName(), rule.getId());
    }

    // ==================== Generation Logic ====================

    /**
     * Synchronizes (generates) transactions for all active rules of the current user.
     * Only creates transactions where scheduledFor <= today.
     * Future transactions are NOT created until their date arrives.
     * Never creates duplicates.
     * 
     * This should be run daily (or on app open) to create transactions as their dates arrive.
     */
    @Transactional
    public SyncResultDto syncTransactions() {
        Long userId = getCurrentUserId();
        List<RecurringRule> activeRules = ruleRepository.findActiveRulesForGeneration(userId);

        int totalCreated = 0;
        int rulesSkipped = 0;
        List<SyncResultDto.RuleSyncDetail> details = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (RecurringRule rule : activeRules) {
            SyncResultDto.RuleSyncDetail detail = generateTransactionsForRule(rule, today);
            details.add(detail);
            totalCreated += detail.getTransactionsCreated();
            if (detail.getTransactionsCreated() == 0 && detail.getMessage() != null) {
                rulesSkipped++;
            }
        }

        log.info("Sync completed: {} transactions created from {} rules ({} skipped)",
                totalCreated, activeRules.size(), rulesSkipped);

        return SyncResultDto.builder()
                .transactionsCreated(totalCreated)
                .rulesProcessed(activeRules.size())
                .rulesSkipped(rulesSkipped)
                .details(details)
                .build();
    }

    /**
     * Generates transactions for a single rule where scheduledDate <= today.
     * Only creates transactions for dates that have already passed or are today.
     */
    private SyncResultDto.RuleSyncDetail generateTransactionsForRule(RecurringRule rule, LocalDate today) {
        String message = null;
        int created = 0;

        // Skip variable-date rules - they require user confirmation each month
        if (Boolean.TRUE.equals(rule.getDateIsVariable())) {
            message = "Skipped: variable date rule (requires manual confirmation)";
            return SyncResultDto.RuleSyncDetail.builder()
                    .ruleId(rule.getId())
                    .ruleName(rule.getName())
                    .transactionsCreated(0)
                    .message(message)
                    .build();
        }

        YearMonth startMonth = YearMonth.from(rule.getStartDate());
        YearMonth todayMonth = YearMonth.from(today);
        
        // Get current instance count for FIXED_TERM progress tracking
        long existingCount = instanceRepository.countByRuleId(rule.getId());

        // Generate for each month from rule start up to today's month
        YearMonth month = startMonth;

        while (!month.isAfter(todayMonth)) {
            // Check FIXED_TERM limit
            if (rule.getEndType() == EndType.FIXED_TERM && rule.getTotalOccurrences() != null) {
                if (existingCount + created >= rule.getTotalOccurrences()) {
                    break;
                }
            }

            LocalDate scheduledDate = calculateScheduledDate(rule, month);

            // Only create if scheduledDate <= today (not in the future)
            if (!scheduledDate.isAfter(today)) {
                // Check if ANY instance exists for this rule in this month (not just exact date).
                // This prevents creating duplicate transactions when dayOfMonth changes.
                // E.g., if Nov 8 exists and user changes day to 4, we shouldn't create Nov 4.
                boolean instanceExistsForMonth = instanceRepository.existsByRuleIdAndYearMonth(
                        rule.getId(), month.getYear(), month.getMonthValue());
                
                if (!instanceExistsForMonth) {
                    createTransactionFromRule(rule, scheduledDate, (int) (existingCount + created + 1));
                    created++;
                }
            }

            month = month.plusMonths(1);
        }

        return SyncResultDto.RuleSyncDetail.builder()
                .ruleId(rule.getId())
                .ruleName(rule.getName())
                .transactionsCreated(created)
                .message(message)
                .build();
    }

    /**
     * Creates a transaction (Entry) from a recurring rule.
     */
    private void createTransactionFromRule(RecurringRule rule, LocalDate scheduledDate, int occurrenceIndex) {
        // Create the transaction (Entry)
        Entry entry = new Entry();
        entry.setUser(rule.getUser());
        entry.setCategory(rule.getCategory());
        entry.setType(rule.getDirection());
        entry.setCurrency(rule.getCurrency());
        entry.setDate(scheduledDate);
        entry.setScheduledFor(scheduledDate);
        entry.setRecurringRule(rule);

        // Set amount: use default if available, otherwise 0 for variable amount rules
        if (rule.getAmountDefault() != null) {
            entry.setAmount(rule.getAmountDefault());
        } else {
            // For variable amount rules without default, set to 0 (user must edit)
            entry.setAmount(BigDecimal.ZERO);
        }

        // Build note with rule reference
        String note = rule.getNote();
        if (rule.getEndType() == EndType.FIXED_TERM && rule.getTotalOccurrences() != null) {
            String progressNote = String.format("[%d/%d] %s",
                    occurrenceIndex, rule.getTotalOccurrences(), rule.getName());
            note = note != null ? progressNote + " - " + note : progressNote;
        }
        entry.setNote(note);

        Entry savedEntry = entryRepository.save(entry);

        // Create the instance link
        RecurringInstance instance = new RecurringInstance();
        instance.setRule(rule);
        instance.setTransaction(savedEntry);
        instance.setScheduledFor(scheduledDate);
        instance.setIsManualOverride(false);

        if (rule.getEndType() == EndType.FIXED_TERM) {
            instance.setOccurrenceIndex(occurrenceIndex);
        }

        instanceRepository.save(instance);

        log.debug("Created transaction from rule '{}' for date {}", rule.getName(), scheduledDate);
    }

    /**
     * Calculates the scheduled date for a given month based on rule's day_of_month.
     * Clamps to last day of month if day doesn't exist (e.g., Feb 30 -> Feb 28/29).
     */
    private LocalDate calculateScheduledDate(RecurringRule rule, YearMonth month) {
        int dayOfMonth = rule.getDayOfMonth() != null ? rule.getDayOfMonth() : 1;
        int maxDay = month.lengthOfMonth();
        int actualDay = Math.min(dayOfMonth, maxDay);
        return month.atDay(actualDay);
    }

    // ==================== Apply Rule Changes ====================

    /**
     * Applies rule changes to existing generated transactions.
     * 
     * For FUTURE_ONLY: Updates only future transactions (scheduledFor >= today).
     * For ALL: Updates ALL transactions from startDate to today, including past ones.
     * 
     * Updates include: category, type, currency, amount, and DATE (based on new dayOfMonth).
     */
    private void applyRuleChangesToInstances(RecurringRule rule, ApplyScope scope) {
        List<RecurringInstance> instancesToUpdate;
        LocalDate today = LocalDate.now();

        if (scope == ApplyScope.ALL) {
            // Update ALL non-overridden instances (past and future)
            instancesToUpdate = instanceRepository.findByRuleIdAndIsManualOverrideFalse(rule.getId());
        } else {
            // FUTURE_ONLY: update only future non-overridden instances
            instancesToUpdate = instanceRepository.findFutureNonOverriddenInstances(rule.getId(), today);
        }

        for (RecurringInstance instance : instancesToUpdate) {
            Entry entry = instance.getTransaction();
            
            // Update entry fields from rule
            entry.setCategory(rule.getCategory());
            entry.setType(rule.getDirection());
            entry.setCurrency(rule.getCurrency());
            
            // Only update amount if not variable or if there's a default
            if (!Boolean.TRUE.equals(rule.getAmountIsVariable()) && rule.getAmountDefault() != null) {
                entry.setAmount(rule.getAmountDefault());
            }

            // Update the DATE based on new dayOfMonth (if date is not variable)
            if (!Boolean.TRUE.equals(rule.getDateIsVariable()) && rule.getDayOfMonth() != null) {
                // Get the month from the original scheduled date and apply new dayOfMonth
                YearMonth month = YearMonth.from(instance.getScheduledFor());
                LocalDate newDate = calculateScheduledDate(rule, month);
                
                entry.setDate(newDate);
                entry.setScheduledFor(newDate);
                instance.setScheduledFor(newDate);
                
                instanceRepository.save(instance);
            }

            entryRepository.save(entry);
        }

        log.info("Applied rule changes to {} instances (scope={})", instancesToUpdate.size(), scope);
    }

    /**
     * Deletes future instances that haven't been manually edited.
     */
    private void deleteFutureInstances(Long ruleId) {
        LocalDate today = LocalDate.now();
        List<RecurringInstance> futurInstances = instanceRepository.findFutureInstancesForDeletion(ruleId, today);

        for (RecurringInstance instance : futurInstances) {
            Entry entry = instance.getTransaction();
            instanceRepository.delete(instance);
            entryRepository.delete(entry);
        }

        log.info("Deleted {} future instances for rule {}", futurInstances.size(), ruleId);
    }

    // ==================== Validation ====================

    private void validateRuleDto(EndType endType, Integer totalOccurrences,
                                  Boolean amountIsVariable, BigDecimal amountDefault,
                                  Boolean dateIsVariable, Integer dayOfMonth) {

        // FIXED_TERM requires totalOccurrences
        if (endType == EndType.FIXED_TERM && (totalOccurrences == null || totalOccurrences < 1)) {
            throw new IllegalArgumentException("FIXED_TERM rules require totalOccurrences >= 1");
        }

        // If amount is not variable, we need a default amount
        if (!Boolean.TRUE.equals(amountIsVariable) && amountDefault == null) {
            throw new IllegalArgumentException("Fixed amount rules require amountDefault");
        }

        // If date is not variable, we need a day of month
        if (!Boolean.TRUE.equals(dateIsVariable) && dayOfMonth == null) {
            throw new IllegalArgumentException("Fixed date rules require dayOfMonth");
        }
    }

    // ==================== DTO Mapping ====================

    private RecurringRuleResponseDto mapToDto(RecurringRule rule) {
        long createdCount = instanceRepository.countByRuleId(rule.getId());

        RecurringRuleResponseDto.RecurringRuleResponseDtoBuilder builder = RecurringRuleResponseDto.builder()
                .id(rule.getId())
                .name(rule.getName())
                .kind(rule.getKind())
                .direction(rule.getDirection())
                .currency(rule.getCurrency())
                .amountDefault(rule.getAmountDefault())
                .amountIsVariable(rule.getAmountIsVariable())
                .dayOfMonth(rule.getDayOfMonth())
                .dateIsVariable(rule.getDateIsVariable())
                .startDate(rule.getStartDate())
                .endType(rule.getEndType())
                .totalOccurrences(rule.getTotalOccurrences())
                .note(rule.getNote())
                .isActive(rule.getIsActive())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .createdCount((int) createdCount);

        // Category info
        if (rule.getCategory() != null) {
            builder.categoryId(rule.getCategory().getId())
                    .categoryName(rule.getCategory().getName())
                    .categoryEmoji(rule.getCategory().getEmoji());
        }

        // Computed: next scheduled date
        if (rule.getIsActive() && !Boolean.TRUE.equals(rule.getDateIsVariable())) {
            LocalDate nextDate = calculateNextScheduledDate(rule, (int) createdCount);
            builder.nextScheduledDate(nextDate);
        }

        // Computed: progress for FIXED_TERM
        if (rule.getEndType() == EndType.FIXED_TERM && rule.getTotalOccurrences() != null) {
            builder.progress(createdCount + "/" + rule.getTotalOccurrences());
            int percent = (int) ((createdCount * 100) / rule.getTotalOccurrences());
            builder.progressPercent(Math.min(percent, 100));
        }

        return builder.build();
    }

    /**
     * Calculates the next scheduled date for a rule.
     */
    private LocalDate calculateNextScheduledDate(RecurringRule rule, int createdCount) {
        // For FIXED_TERM rules that have completed all occurrences
        if (rule.getEndType() == EndType.FIXED_TERM &&
                rule.getTotalOccurrences() != null &&
                createdCount >= rule.getTotalOccurrences()) {
            return null;
        }

        // Guard against null startDate
        if (rule.getStartDate() == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        YearMonth startMonth = YearMonth.from(rule.getStartDate());
        YearMonth currentMonth = YearMonth.from(today);

        // If the rule hasn't started yet, return the first scheduled date
        if (startMonth.isAfter(currentMonth)) {
            return calculateScheduledDate(rule, startMonth);
        }

        // Calculate the scheduled date for the current month
        LocalDate currentMonthScheduled = calculateScheduledDate(rule, currentMonth);

        // If today is before or on the scheduled day this month, return this month's date
        // Otherwise, return next month's date
        if (!today.isAfter(currentMonthScheduled)) {
            return currentMonthScheduled;
        } else {
            return calculateScheduledDate(rule, currentMonth.plusMonths(1));
        }
    }

    // ==================== Instance Operations ====================

    public List<RecurringInstanceResponseDto> getInstancesForRule(Long ruleId) {
        Long userId = getCurrentUserId();
        RecurringRule rule = ruleRepository.findByIdAndUserId(ruleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring rule not found"));

        List<RecurringInstance> instances = instanceRepository.findByRuleIdOrderByScheduledForAsc(rule.getId());
        return instances.stream()
                .map(this::mapInstanceToDto)
                .collect(Collectors.toList());
    }

    /**
     * Marks a transaction as manually overridden.
     * Called when user edits a generated transaction directly.
     */
    @Transactional
    public void markAsManualOverride(Long transactionId) {
        instanceRepository.findByTransactionId(transactionId)
                .ifPresent(instance -> {
                    instance.setIsManualOverride(true);
                    instanceRepository.save(instance);
                    log.debug("Marked transaction {} as manually overridden", transactionId);
                });
    }

    private RecurringInstanceResponseDto mapInstanceToDto(RecurringInstance instance) {
        return RecurringInstanceResponseDto.builder()
                .id(instance.getId())
                .ruleId(instance.getRule().getId())
                .ruleName(instance.getRule().getName())
                .transactionId(instance.getTransaction().getId())
                .scheduledFor(instance.getScheduledFor())
                .occurrenceIndex(instance.getOccurrenceIndex())
                .isManualOverride(instance.getIsManualOverride())
                .createdAt(instance.getCreatedAt())
                .build();
    }
}
