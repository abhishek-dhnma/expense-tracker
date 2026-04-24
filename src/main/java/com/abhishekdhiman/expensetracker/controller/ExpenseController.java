package com.abhishekdhiman.expensetracker.controller;

import com.abhishekdhiman.expensetracker.dto.ExpenseDTO;
import com.abhishekdhiman.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ExpenseDTO expenseDTO) {
        
        log.info("Received request to create expense. Idempotency Key: {}", idempotencyKey);
        ExpenseDTO createdExpense = expenseService.createExpense(expenseDTO, idempotencyKey);
        return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "date_desc") String sort) {
        
        log.info("Received request to fetch expenses");
        List<ExpenseDTO> expenses = expenseService.getExpenses(category, sort);
        return ResponseEntity.ok(expenses);
    }
}
