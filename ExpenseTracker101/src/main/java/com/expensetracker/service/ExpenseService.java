package com.expensetracker.service;

import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> getAllExpensesForUser(String userId) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId);
    }

    public List<Expense> getExpensesByCategory(String userId, String categoryId) {
        return expenseRepository.findByUserIdAndCategoryId(userId, categoryId);
    }

    public List<Expense> getExpensesByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    public List<Expense> getExpensesByDateRangeAndCategory(
            String userId, LocalDate startDate, LocalDate endDate, String categoryId) {
        return expenseRepository.findByUserIdAndDateBetweenAndCategoryId(
                userId, startDate, endDate, categoryId);
    }

    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public Optional<Expense> getExpenseById(String id) {
        return expenseRepository.findById(id);
    }

    public Expense updateExpense(Expense expense) {
        // Verify the expense exists
        Expense existingExpense = expenseRepository.findById(expense.getId())
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        existingExpense.setDescription(expense.getDescription());
        existingExpense.setAmount(expense.getAmount());
        existingExpense.setCategoryId(expense.getCategoryId());
        existingExpense.setDate(expense.getDate());

        return expenseRepository.save(existingExpense);
    }

    public void deleteExpense(String id) {
        expenseRepository.deleteById(id);
    }

    public BigDecimal getTotalExpenses(String userId) {
        return getAllExpensesForUser(userId).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> getMonthlyExpenses(String userId, int year) {
        Map<String, BigDecimal> monthlyExpenses = new HashMap<>();

        for (int monthValue = 1; monthValue <= 12; monthValue++) {
            LocalDate startDate = LocalDate.of(year, monthValue, 1);
            LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

            List<Expense> expenses = getExpensesByDateRange(userId, startDate, endDate);
            BigDecimal total = expenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthlyExpenses.put(startDate.getMonth().toString(), total);
        }

        return monthlyExpenses;
    }

    public Map<String, BigDecimal> getExpensesByCategory(String userId) {
        Map<String, BigDecimal> categoryExpenses = new HashMap<>();

        List<Expense> expenses = getAllExpensesForUser(userId);

        for (Expense expense : expenses) {
            String categoryId = expense.getCategoryId();
            BigDecimal amount = expense.getAmount();

            categoryExpenses.put(categoryId,
                    categoryExpenses.getOrDefault(categoryId, BigDecimal.ZERO).add(amount));
        }

        return categoryExpenses;
    }
}