package com.processedfood.inventory.dto;

import com.processedfood.inventory.model.StockType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockRequest {
    @NotNull
    private Long productId;

    @NotNull
    private StockType type;

    @NotNull
    @Min(1)
    private Integer quantity;

    private String referenceNote;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public StockType getType() {
        return type;
    }

    public void setType(StockType type) {
        this.type = type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReferenceNote() {
        return referenceNote;
    }

    public void setReferenceNote(String referenceNote) {
        this.referenceNote = referenceNote;
    }
}
