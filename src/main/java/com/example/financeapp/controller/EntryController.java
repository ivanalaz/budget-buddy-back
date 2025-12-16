package com.example.financeapp.controller;

import com.example.financeapp.dto.CreateEntryRequestDto;
import com.example.financeapp.dto.EntryResponseDto;
import com.example.financeapp.dto.UpdateEntryRequestDto;
import com.example.financeapp.entity.CurrencyCode;
import com.example.financeapp.service.EntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    @GetMapping
    public ResponseEntity<List<EntryResponseDto>> getEntries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CurrencyCode currency) {
        List<EntryResponseDto> entries = entryService.getEntries(
                from, to, Optional.ofNullable(categoryId), Optional.ofNullable(currency)
        );
        return ResponseEntity.ok(entries);
    }

    @PostMapping
    public ResponseEntity<EntryResponseDto> createEntry(@Valid @RequestBody CreateEntryRequestDto dto) {
        EntryResponseDto created = entryService.createEntry(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntryResponseDto> getEntry(@PathVariable Long id) {
        EntryResponseDto entry = entryService.getEntry(id);
        return ResponseEntity.ok(entry);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntryResponseDto> updateEntry(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEntryRequestDto dto) {
        EntryResponseDto updated = entryService.updateEntry(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        entryService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }
}

