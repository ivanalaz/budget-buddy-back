package com.example.financeapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResultDto {

    /**
     * Total number of transactions created during this sync.
     */
    private Integer transactionsCreated;

    /**
     * Number of rules that were processed.
     */
    private Integer rulesProcessed;

    /**
     * Number of rules that were skipped (e.g., variable date rules).
     */
    private Integer rulesSkipped;

    /**
     * Details of created transactions by rule.
     */
    private List<RuleSyncDetail> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleSyncDetail {
        private Long ruleId;
        private String ruleName;
        private Integer transactionsCreated;
        private String message;
    }
}
