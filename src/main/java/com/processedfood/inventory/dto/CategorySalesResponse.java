package com.processedfood.inventory.dto;

public class CategorySalesResponse {
    private Long categoryId;
    private String categoryName;
    private Long soldQuantity;

    public CategorySalesResponse(Long categoryId, String categoryName, Long soldQuantity) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.soldQuantity = soldQuantity;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Long getSoldQuantity() {
        return soldQuantity;
    }
}
