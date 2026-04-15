package com.expensetracker.service;

import com.expensetracker.model.Category;
import com.expensetracker.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void createDefaultCategories() {
        // Check if default categories already exist
        if (categoryRepository.findByUserIdOrIsDefaultIsTrue(null).isEmpty()) {
            List<String> defaultCategories = Arrays.asList(
                    "Food", "Transportation", "Entertainment",
                    "Utilities", "Housing", "Healthcare",
                    "Education", "Shopping", "Personal Care",
                    "Gifts", "Travel", "Miscellaneous"
            );

            for (String categoryName : defaultCategories) {
                Category category = new Category(categoryName, null, true);
                categoryRepository.save(category);
            }
        }
    }

    public List<Category> getCategoriesForUser(String userId) {
        return categoryRepository.findByUserIdOrIsDefaultIsTrue(userId);
    }

    public Category createCategory(Category category) {
        // Check if category already exists for this user
        Optional<Category> existingCategory = categoryRepository.findByNameAndUserId(
                category.getName(), category.getUserId());

        if (existingCategory.isPresent()) {
            throw new IllegalArgumentException("Category already exists");
        }

        return categoryRepository.save(category);
    }

    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    public Category updateCategory(Category category) {
        // Verify the category exists
        Category existingCategory = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Don't allow updating default categories
        if (existingCategory.isDefault()) {
            throw new IllegalArgumentException("Cannot update default category");
        }

        // Check if new name doesn't conflict with existing categories
        if (!existingCategory.getName().equals(category.getName())) {
            Optional<Category> nameConflict = categoryRepository.findByNameAndUserId(
                    category.getName(), category.getUserId());

            if (nameConflict.isPresent()) {
                throw new IllegalArgumentException("Category name already exists");
            }
        }

        existingCategory.setName(category.getName());
        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(String id) {
        // Verify the category exists
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Don't allow deleting default categories
        if (category.isDefault()) {
            throw new IllegalArgumentException("Cannot delete default category");
        }

        categoryRepository.deleteById(id);
    }
}