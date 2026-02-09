package com.example.financeapp.controller;

import com.example.financeapp.dto.*;
import com.example.financeapp.entity.ApplyScope;
import com.example.financeapp.service.RecurringRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-rules")
@RequiredArgsConstructor
public class RecurringRuleController {

    private final RecurringRuleService recurringRuleService;

    /**
     * Get all recurring rules for the current user.
     * Returns rules with computed fields: nextScheduledDate, progress, createdCount.
     */
    @GetMapping
    public ResponseEntity<List<RecurringRuleResponseDto>> getAllRules() {
        List<RecurringRuleResponseDto> rules = recurringRuleService.getAllRules();
        return ResponseEntity.ok(rules);
    }

    /**
     * Get a specific recurring rule by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecurringRuleResponseDto> getRule(@PathVariable Long id) {
        RecurringRuleResponseDto rule = recurringRuleService.getRule(id);
        return ResponseEntity.ok(rule);
    }

    /**
     * Create a new recurring rule.
     */
    @PostMapping
    public ResponseEntity<RecurringRuleResponseDto> createRule(
            @Valid @RequestBody CreateRecurringRuleRequestDto dto) {
        RecurringRuleResponseDto created = recurringRuleService.createRule(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing recurring rule.
     *
     * @param id         The rule ID to update
     * @param dto        The updated rule data
     * @param applyScope How to apply changes to existing generated transactions:
     *                   - FUTURE_ONLY (default): Only affects future transactions
     *                   - ALL: Updates all generated transactions (past and future),
     *                          but preserves manually edited ones
     */
    @PutMapping("/{id}")
    public ResponseEntity<RecurringRuleResponseDto> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecurringRuleRequestDto dto,
            @RequestParam(defaultValue = "FUTURE_ONLY") ApplyScope applyScope) {
        RecurringRuleResponseDto updated = recurringRuleService.updateRule(id, dto, applyScope);
        return ResponseEntity.ok(updated);
    }

    /**
     * Toggle the active status of a recurring rule.
     * When deactivating, optionally delete future generated transactions.
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<RecurringRuleResponseDto> toggleActive(
            @PathVariable Long id,
            @Valid @RequestBody ToggleActiveRequestDto dto) {
        RecurringRuleResponseDto updated = recurringRuleService.toggleActive(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete (soft-delete) a recurring rule.
     * Sets isActive to false. Optionally deletes future generated transactions.
     *
     * @param id                    The rule ID to delete
     * @param deleteFutureGenerated If true, delete future generated transactions
     *                              that haven't been manually edited
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean deleteFutureGenerated) {
        recurringRuleService.deleteRule(id, deleteFutureGenerated);
        return ResponseEntity.noContent().build();
    }

    /**
     * Sync/generate transactions for all active recurring rules.
     * Creates missing transactions from start_date up to current month + 1 month ahead.
     * Never creates duplicates.
     *
     * This endpoint can be called:
     * - On app start
     * - When opening the recurring transactions screen
     * - Periodically via a scheduled job
     */
    @PostMapping("/sync")
    public ResponseEntity<SyncResultDto> syncTransactions() {
        SyncResultDto result = recurringRuleService.syncTransactions();
        return ResponseEntity.ok(result);
    }

    /**
     * Get all generated instances (transactions) for a specific rule.
     * Useful for viewing the history of a recurring rule.
     */
    @GetMapping("/{id}/instances")
    public ResponseEntity<List<RecurringInstanceResponseDto>> getInstances(@PathVariable Long id) {
        List<RecurringInstanceResponseDto> instances = recurringRuleService.getInstancesForRule(id);
        return ResponseEntity.ok(instances);
    }
}
