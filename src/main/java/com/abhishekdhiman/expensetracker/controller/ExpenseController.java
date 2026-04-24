package com.abhishekdhiman.expensetracker.controller;

import com.abhishekdhiman.expensetracker.model.Expense;
import com.abhishekdhiman.expensetracker.repository.ExpenseRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final ConcurrentHashMap<String, Expense> idempotencyCache = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<Expense> createExpense(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody Expense expense) {
        
        if (idempotencyKey != null && idempotencyCache.containsKey(idempotencyKey)) {
            return new ResponseEntity<>(idempotencyCache.get(idempotencyKey), HttpStatus.OK);
        }

        Expense savedExpense = expenseRepository.save(expense);
        
        if (idempotencyKey != null) {
            idempotencyCache.put(idempotencyKey, savedExpense);
        }
        
        return new ResponseEntity<>(savedExpense, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "date_desc") String sort) {
        
        Sort.Direction direction = sort.equals("date_asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortOrder = Sort.by(direction, "date");

        List<Expense> expenses;
        if (category != null && !category.trim().isEmpty()) {
            expenses = expenseRepository.findByCategory(category, sortOrder);
        } else {
            expenses = expenseRepository.findAll(sortOrder);
        }

        return ResponseEntity.ok(expenses);
    }
}
