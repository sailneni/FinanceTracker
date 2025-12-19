package com.financetracker.dto;

import com.financetracker.entity.Category;
import com.financetracker.entity.TansactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionRequest {

    private TansactionType type;
    private Category category;
    private BigDecimal amount;
    private String description;
    private LocalDateTime dateTime;

    public TansactionType getType() {
        return type;
    }

    public void setType(TansactionType type) {
        this.type = type;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
