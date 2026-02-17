package com.processedfood.inventory.service;

import com.processedfood.inventory.dto.InvoiceItemRequest;
import com.processedfood.inventory.dto.InvoiceRequest;
import com.processedfood.inventory.dto.InvoiceSummaryResponse;
import com.processedfood.inventory.exception.BadRequestException;
import com.processedfood.inventory.model.*;
import com.processedfood.inventory.repository.InvoiceRepository;
import com.processedfood.inventory.repository.ProductRepository;
import com.processedfood.inventory.repository.StockTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ProductRepository productRepository,
                          StockTransactionRepository stockTransactionRepository) {
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.stockTransactionRepository = stockTransactionRepository;
    }

    public List<Invoice> listRecent() {
        return invoiceRepository.findTop20ByOrderByDateDesc();
    }

    public List<InvoiceSummaryResponse> listRecentSummaries() {
        return invoiceRepository.findTop20ByOrderByDateDesc().stream()
                .map(i -> new InvoiceSummaryResponse(
                        i.getId(),
                        i.getInvoiceNumber(),
                        i.getDate(),
                        i.getCustomerName(),
                        i.getTotalAmount()
                ))
                .toList();
    }

    public Invoice findById(Long id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Invoice not found"));
    }

    @Transactional
    public Invoice create(InvoiceRequest request) {
        Invoice invoice = new Invoice();
        invoice.setCustomerName(request.getCustomerName());
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        BigDecimal total = BigDecimal.ZERO;

        for (InvoiceItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new NoSuchElementException("Product not found"));

            if (product.getQuantity() < itemReq.getQuantity()) {
                throw new BadRequestException("Insufficient stock for " + product.getName());
            }

            product.setQuantity(product.getQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(product.getSellingPrice());
            item.setSubtotal(product.getSellingPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            invoice.getInvoiceItems().add(item);
            total = total.add(item.getSubtotal());

            StockTransaction tx = new StockTransaction();
            tx.setProduct(product);
            tx.setType(StockType.OUT);
            tx.setQuantity(itemReq.getQuantity());
            tx.setReferenceNote("Invoice " + invoice.getInvoiceNumber());
            stockTransactionRepository.save(tx);
        }

        invoice.setTotalAmount(total);
        return invoiceRepository.save(invoice);
    }

    public BigDecimal monthTotal(YearMonth yearMonth) {
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        return invoiceRepository.findByDateGreaterThanEqualAndDateLessThan(start, end).stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalSalesToday() {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        return invoiceRepository.findAll().stream()
                .filter(inv -> inv.getDate().isAfter(start))
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void deleteInvoice(Long id) {
        Invoice invoice = findById(id);
        for (InvoiceItem item : invoice.getInvoiceItems()) {
            Product product = item.getProduct();
            product.setQuantity((product.getQuantity() == null ? 0 : product.getQuantity()) + item.getQuantity());
            productRepository.save(product);
        }
        stockTransactionRepository.deleteByReferenceNote("Invoice " + invoice.getInvoiceNumber());
        invoiceRepository.delete(invoice);
    }
}
