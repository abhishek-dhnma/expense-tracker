package com.abhishekdhiman.expensetracker.controller;

import com.abhishekdhiman.expensetracker.dto.ExpenseDTO;
import com.abhishekdhiman.expensetracker.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
public class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetExpenses() throws Exception {
        ExpenseDTO e1 = new ExpenseDTO();
        e1.setId(1L);
        e1.setAmount(BigDecimal.valueOf(100.0));
        e1.setCategory("Food");
        e1.setDate(LocalDate.now());

        ExpenseDTO e2 = new ExpenseDTO();
        e2.setId(2L);
        e2.setAmount(BigDecimal.valueOf(50.0));
        e2.setCategory("Transport");
        e2.setDate(LocalDate.now());

        Mockito.when(expenseService.getExpenses(isNull(), eq("date_desc"))).thenReturn(Arrays.asList(e1, e2));

        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("Food"))
                .andExpect(jsonPath("$[1].category").value("Transport"));
    }

    @Test
    public void testCreateExpense() throws Exception {
        ExpenseDTO expense = new ExpenseDTO();
        expense.setAmount(BigDecimal.valueOf(150.0));
        expense.setCategory("Utilities");
        expense.setDate(LocalDate.now());

        ExpenseDTO savedExpense = new ExpenseDTO();
        savedExpense.setId(3L);
        savedExpense.setAmount(BigDecimal.valueOf(150.0));
        savedExpense.setCategory("Utilities");
        savedExpense.setDate(expense.getDate());

        Mockito.when(expenseService.createExpense(any(ExpenseDTO.class), isNull())).thenReturn(savedExpense);

        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.category").value("Utilities"));
    }
}
