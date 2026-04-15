package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {

    List<Expense> findByUserId(String userId);

    List<Expense> findByUserIdOrderByDateDesc(String userId);

    List<Expense> findByUserIdAndCategoryId(String userId, String categoryId);

    List<Expense> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    @Query("{'userId': ?0, 'date': {'$gte': ?1, '$lte': ?2}, 'categoryId': ?3}")
    List<Expense> findByUserIdAndDateBetweenAndCategoryId(
            String userId, LocalDate startDate, LocalDate endDate, String categoryId);

    void deleteByUserId(String userId);
}