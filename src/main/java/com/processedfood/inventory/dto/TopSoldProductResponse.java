package com.processedfood.inventory.dto;

public class TopSoldProductResponse {
    private Long productId;
    private String productName;
    private String imagePath;
    private Long soldQuantity;

    public TopSoldProductResponse(Long productId, String productName, String imagePath, Long soldQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.imagePath = imagePath;
        this.soldQuantity = soldQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Long getSoldQuantity() {
        return soldQuantity;
    }
}
