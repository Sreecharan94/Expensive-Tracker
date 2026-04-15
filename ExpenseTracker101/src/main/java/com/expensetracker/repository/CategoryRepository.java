package com.expensetracker.repository;

import com.expensetracker.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category, String> {

    List<Category> findByUserId(String userId);

    List<Category> findByUserIdOrIsDefaultIsTrue(String userId);

    Optional<Category> findByNameAndUserId(String name, String userId);

    void deleteByUserId(String userId);
}