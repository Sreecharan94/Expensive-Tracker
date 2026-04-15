package com.expensetracker.controller;

import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.util.ExportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final ExportUtil exportUtil;

    @Autowired
    public ReportController(ExpenseService expenseService, CategoryService categoryService, ExportUtil exportUtil) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
        this.exportUtil = exportUtil;
    }

    @GetMapping
    public String showReports(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String categoryId,
            Model model) {

        // Default to current month
        if (startDate == null) {
            YearMonth currentMonth = YearMonth.now();
            startDate = currentMonth.atDay(1);
            endDate = currentMonth.atEndOfMonth();
        } else if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Get expenses
        List<Expense> expenses = (categoryId != null && !categoryId.isEmpty()) ?
                expenseService.getExpensesByDateRangeAndCategory(user.getId(), startDate, endDate, categoryId)
                : expenseService.getExpensesByDateRange(user.getId(), startDate, endDate);

        // Get user categories
        List<Category> categories = categoryService.getCategoriesForUser(user.getId());

        // Map for category names
        Map<String, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // Total expenses
        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Average daily expenses (based on days in selected range)
        long days = startDate.until(endDate).getDays() + 1;
        BigDecimal averageDailyExpense = days > 0 ?
                totalExpenses.divide(BigDecimal.valueOf(days), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        // Group by category
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        for (Expense expense : expenses) {
            String catId = expense.getCategoryId();
            expensesByCategory.merge(catId, expense.getAmount(), BigDecimal::add);
        }

        model.addAttribute("expenses", expenses);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryMap", categoryMap);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("averageDailyExpense", averageDailyExpense);
        model.addAttribute("expensesByCategory", expensesByCategory);

        return "reports/index";
    }

    @GetMapping("/export/{format}")
    public ResponseEntity<byte[]> exportReport(
            @AuthenticationPrincipal User user,
            @PathVariable String format,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String categoryId) {

        // Get expenses
        List<Expense> expenses = (categoryId != null && !categoryId.isEmpty()) ?
                expenseService.getExpensesByDateRangeAndCategory(user.getId(), startDate, endDate, categoryId)
                : expenseService.getExpensesByDateRange(user.getId(), startDate, endDate);

        // Category names
        List<Category> categories = categoryService.getCategoriesForUser(user.getId());
        Map<String, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // Filename
        String filename = "expense-report-" + startDate + "-to-" + endDate;

        byte[] content;
        HttpHeaders headers = new HttpHeaders();

        switch (format.toLowerCase()) {
            case "pdf":
                content = exportUtil.exportToPdf(expenses, categoryMap, startDate, endDate);
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData(filename + ".pdf", filename + ".pdf");
                break;

            case "csv":
                content = exportUtil.exportToCsv(expenses, categoryMap);
                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDispositionFormData(filename + ".csv", filename + ".csv");
                break;

            case "excel":
                content = exportUtil.exportToExcel(expenses, categoryMap, startDate, endDate);
                headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
                headers.setContentDispositionFormData(filename + ".xlsx", filename + ".xlsx");
                break;

            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }

        return ResponseEntity.ok().headers(headers).body(content);
    }
}
