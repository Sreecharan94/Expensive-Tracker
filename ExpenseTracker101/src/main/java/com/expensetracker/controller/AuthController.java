package com.expensetracker.controller;

import com.expensetracker.model.User;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;
    private final CategoryService categoryService;

    @Autowired
    public AuthController(UserService userService, CategoryService categoryService) {
        this.userService = userService;
        this.categoryService = categoryService;
    }

    /**
     * Displays the login page.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Displays the registration form.
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Handles user registration.
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result, Model model) {
        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("error", "Please correct the errors below.");
            return "register";
        }

        try {
            // Register the user
            userService.registerUser(user);

            // Create default categories for the system if not already created
            categoryService.createDefaultCategories();

            // Redirect to login page with success message
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            // Handle registration errors
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}