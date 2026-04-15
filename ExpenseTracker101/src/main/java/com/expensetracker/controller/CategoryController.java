package com.expensetracker.controller;

import com.expensetracker.model.Category;
import com.expensetracker.model.User;
import com.expensetracker.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // List all categories for the logged-in user
    @GetMapping
    public String getAllCategories(@AuthenticationPrincipal User user, Model model) {
        List<Category> categories = categoryService.getCategoriesForUser(user.getId());
        model.addAttribute("categories", categories);
        return "categories/list";
    }

    // Show add form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/add";
    }

    // Handle add submission
    @PostMapping("/add")
    public String addCategory(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("category") Category category,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "categories/add";
        }

        category.setUserId(user.getId());
        category.setDefault(false);

        try {
            categoryService.createCategory(category);
            redirectAttributes.addFlashAttribute("message", "Category added successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/categories";
    }

    // Show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + id));

        if (category.isDefault() || !user.getId().equals(category.getUserId())) {
            redirectAttributes.addFlashAttribute("error", "You cannot edit this category");
            return "redirect:/categories";
        }

        model.addAttribute("category", category);
        return "categories/edit";
    }

    // Handle edit submission
    @PostMapping("/edit/{id}")
    public String updateCategory(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @Valid @ModelAttribute("category") Category category,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "categories/edit";
        }

        category.setId(id);
        category.setUserId(user.getId());
        category.setDefault(false);

        try {
            categoryService.updateCategory(category);
            redirectAttributes.addFlashAttribute("message", "Category updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/categories";
    }

    // Handle deletion
    @GetMapping("/delete/{id}")
    public String deleteCategory(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {

        try {
            Category category = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + id));

            if (category.isDefault() || !user.getId().equals(category.getUserId())) {
                redirectAttributes.addFlashAttribute("error", "You cannot delete this category");
                return "redirect:/categories";
            }

            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("message", "Category deleted successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/categories";
    }
}
