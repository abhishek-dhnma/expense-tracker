package com.abhishekdhiman.expensetracker.controller;

import com.abhishekdhiman.expensetracker.model.Expense;
import com.abhishekdhiman.expensetracker.repository.ExpenseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
public class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseRepository expenseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetExpenses() throws Exception {
        Expense e1 = new Expense();
        e1.setId(1L);
        e1.setAmount(BigDecimal.valueOf(100.0));
        e1.setCategory("Food");
        e1.setDate(LocalDate.now());

        Expense e2 = new Expense();
        e2.setId(2L);
        e2.setAmount(BigDecimal.valueOf(50.0));
        e2.setCategory("Transport");
        e2.setDate(LocalDate.now());

        Mockito.when(expenseRepository.findAll(any(Sort.class))).thenReturn(Arrays.asList(e1, e2));

        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("Food"))
                .andExpect(jsonPath("$[1].category").value("Transport"));
    }

    @Test
    public void testCreateExpense() throws Exception {
        Expense expense = new Expense();
        expense.setAmount(BigDecimal.valueOf(150.0));
        expense.setCategory("Utilities");
        expense.setDate(LocalDate.now());

        Expense savedExpense = new Expense();
        savedExpense.setId(3L);
        savedExpense.setAmount(BigDecimal.valueOf(150.0));
        savedExpense.setCategory("Utilities");
        savedExpense.setDate(expense.getDate());

        Mockito.when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.category").value("Utilities"));
    }
}
