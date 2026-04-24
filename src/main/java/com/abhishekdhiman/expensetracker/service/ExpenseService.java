package com.abhishekdhiman.expensetracker.service;

import com.abhishekdhiman.expensetracker.dto.ExpenseDTO;
import com.abhishekdhiman.expensetracker.model.Expense;
import com.abhishekdhiman.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    // In-memory idempotency cache (For production, this would be Redis)
    private final ConcurrentHashMap<String, Expense> idempotencyCache = new ConcurrentHashMap<>();

    public ExpenseDTO createExpense(ExpenseDTO expenseDTO, String idempotencyKey) {
        if (idempotencyKey != null && idempotencyCache.containsKey(idempotencyKey)) {
            log.info("Idempotency key hit! Returning cached expense for key: {}", idempotencyKey);
            return mapToDTO(idempotencyCache.get(idempotencyKey));
        }

        log.info("Creating new expense: {}", expenseDTO.getCategory());
        Expense expense = mapToEntity(expenseDTO);
        Expense savedExpense = expenseRepository.save(expense);

        if (idempotencyKey != null) {
            idempotencyCache.put(idempotencyKey, savedExpense);
        }

        return mapToDTO(savedExpense);
    }

    public List<ExpenseDTO> getExpenses(String category, String sort) {
        log.info("Fetching expenses. Category filter: {}, Sort: {}", category, sort);
        
        Sort.Direction direction = sort.equals("date_asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortOrder = Sort.by(direction, "date");

        List<Expense> expenses;
        if (category != null && !category.trim().isEmpty()) {
            expenses = expenseRepository.findByCategory(category, sortOrder);
        } else {
            expenses = expenseRepository.findAll(sortOrder);
        }

        return expenses.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Helper methods for mapping
    private ExpenseDTO mapToDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setAmount(expense.getAmount());
        dto.setCategory(expense.getCategory());
        dto.setDescription(expense.getDescription());
        dto.setDate(expense.getDate());
        dto.setCreatedAt(expense.getCreatedAt());
        return dto;
    }

    private Expense mapToEntity(ExpenseDTO dto) {
        Expense expense = new Expense();
        expense.setAmount(dto.getAmount());
        expense.setCategory(dto.getCategory());
        expense.setDescription(dto.getDescription());
        expense.setDate(dto.getDate());
        return expense;
    }
}
