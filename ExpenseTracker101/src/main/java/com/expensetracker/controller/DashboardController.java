package com.expensetracker.controller;

import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;

    @Autowired
    public DashboardController(ExpenseService expenseService, CategoryService categoryService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        // Debug output for authenticated user
        System.out.println("Authenticated user: " + user);

        // Default to current month if no date range is provided
        if (startDate == null) {
            YearMonth currentMonth = YearMonth.now();
            startDate = currentMonth.atDay(1);
            endDate = currentMonth.atEndOfMonth();
        } else if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Fetch expenses within the date range
        List<Expense> expenses = expenseService.getExpensesByDateRange(user.getId(), startDate, endDate);

        // Fetch categories
        List<Category> categories = categoryService.getCategoriesForUser(user.getId());

        // Map categoryId to categoryName
        Map<String, String> categoryMap = new HashMap<>();
        for (Category category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }

        // Calculate total expenses
        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate expenses by category
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        for (Expense expense : expenses) {
            String categoryId = expense.getCategoryId();
            BigDecimal amount = expense.getAmount();
            expensesByCategory.merge(categoryId, amount, BigDecimal::add);
        }

        // Add all data to the model
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("expenses", expenses);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryMap", categoryMap);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("expensesByCategory", expensesByCategory);

        return "dashboard";
    }
}
