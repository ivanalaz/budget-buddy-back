package com.example.financeapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequestDto {
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    private String emoji;
}

