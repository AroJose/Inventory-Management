package com.processedfood.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceSummaryResponse {
    private Long id;
    private String invoiceNumber;
    private LocalDateTime date;
    private String customerName;
    private BigDecimal totalAmount;

    public InvoiceSummaryResponse(Long id, String invoiceNumber, LocalDateTime date, String customerName, BigDecimal totalAmount) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.date = date;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
    }

    public Long getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getCustomerName() {
        return customerName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
