package com.expensetracker.controller;

import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;

    @Autowired
    public ExpenseController(ExpenseService expenseService, CategoryService categoryService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String getAllExpenses(@AuthenticationPrincipal User user, Model model) {
        List<Expense> expenses = expenseService.getAllExpensesForUser(user.getId());
        List<Category> categories = categoryService.getCategoriesForUser(user.getId());

        model.addAttribute("expenses", expenses);
        model.addAttribute("categories", categories);
        return "expenses/list";
    }

    @GetMapping("/add")
    public String showAddForm(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("expense", new Expense());
        model.addAttribute("categories", categoryService.getCategoriesForUser(user.getId()));
        return "expenses/add";
    }

    @PostMapping("/add")
    public String addExpense(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("expense") Expense expense,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getCategoriesForUser(user.getId()));
            return "expenses/add";
        }

        // Set the user ID before saving
        expense.setUserId(user.getId());

        // If no date is selected, use today's date
        if (expense.getDate() == null) {
            expense.setDate(LocalDate.now());
        }

        expenseService.addExpense(expense);
        redirectAttributes.addFlashAttribute("message", "Expense added successfully");
        return "redirect:/expenses";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            Model model) {

        Expense expense = expenseService.getExpenseById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid expense ID: " + id));

        // Verify that the expense belongs to the current user
        if (!expense.getUserId().equals(user.getId())) {
            return "redirect:/expenses";
        }

        model.addAttribute("expense", expense);
        model.addAttribute("categories", categoryService.getCategoriesForUser(user.getId()));
        return "expenses/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateExpense(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @Valid @ModelAttribute("expense") Expense expense,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getCategoriesForUser(user.getId()));
            return "expenses/edit";
        }

        // Ensure the expense ID and user ID are set correctly
        expense.setId(id);
        expense.setUserId(user.getId());

        try {
            expenseService.updateExpense(expense);
            redirectAttributes.addFlashAttribute("message", "Expense updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/expenses";
    }

    @GetMapping("/delete/{id}")
    public String deleteExpense(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {

        // Verify the expense exists and belongs to the current user
        Expense expense = expenseService.getExpenseById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid expense ID: " + id));

        if (!expense.getUserId().equals(user.getId())) {
            return "redirect:/expenses";
        }

        expenseService.deleteExpense(id);
        redirectAttributes.addFlashAttribute("message", "Expense deleted successfully");
        return "redirect:/expenses";
    }

    @GetMapping("/filter")
    public String filterExpenses(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        List<Expense> expenses;

        if (categoryId != null && !categoryId.isEmpty() && startDate != null && endDate != null) {
            // Filter by both category and date range
            expenses = expenseService.getExpensesByDateRangeAndCategory(
                    user.getId(), startDate, endDate, categoryId);
        } else if (categoryId != null && !categoryId.isEmpty()) {
            // Filter by category only
            expenses = expenseService.getExpensesByCategory(user.getId(), categoryId);
        } else if (startDate != null && endDate != null) {
            // Filter by date range only
            expenses = expenseService.getExpensesByDateRange(user.getId(), startDate, endDate);
        } else {
            // No filters applied, get all expenses
            expenses = expenseService.getAllExpensesForUser(user.getId());
        }

        List<Category> categories = categoryService.getCategoriesForUser(user.getId());

        model.addAttribute("expenses", expenses);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "expenses/list";
    }
}