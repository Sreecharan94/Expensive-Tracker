package com.expensetracker.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "expenses")
public class Expense {

    @Id
    private String id;

    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private String categoryId;
    private String userId;

    // Constructors
    public Expense() {
    }

    public Expense(String description, BigDecimal amount, String categoryId,
                   LocalDate date, String userId) {
        this.description = description;
        this.amount = amount;
        this.categoryId = categoryId;
        this.date = date;
        this.userId = userId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", categoryId='" + categoryId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
