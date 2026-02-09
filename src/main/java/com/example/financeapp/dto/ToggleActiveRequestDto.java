package com.example.financeapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToggleActiveRequestDto {

    @NotNull(message = "isActive is required")
    private Boolean isActive;

    /**
     * If true and isActive is being set to false, delete future generated
     * transactions that haven't been manually edited.
     */
    private Boolean deleteFutureGenerated = false;
}
