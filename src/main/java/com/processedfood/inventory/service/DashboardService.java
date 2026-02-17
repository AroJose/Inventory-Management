package com.processedfood.inventory.service;

import com.processedfood.inventory.dto.TopSoldProductResponse;
import com.processedfood.inventory.model.Product;
import com.processedfood.inventory.repository.InvoiceItemRepository;
import com.processedfood.inventory.repository.CategoryRepository;
import com.processedfood.inventory.repository.ProductRepository;
import com.processedfood.inventory.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.TextStyle;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductService productService;
    private final InvoiceService invoiceService;
    private final InvoiceItemRepository invoiceItemRepository;

    public DashboardService(ProductRepository productRepository,
                            CategoryRepository categoryRepository,
                            SupplierRepository supplierRepository,
                            ProductService productService,
                            InvoiceService invoiceService,
                            InvoiceItemRepository invoiceItemRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productService = productService;
        this.invoiceService = invoiceService;
        this.invoiceItemRepository = invoiceItemRepository;
    }

    public Map<String, Object> summary() {
        List<Product> products = productRepository.findAll();
        BigDecimal stockValue = products.stream()
                .map(p -> p.getSellingPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalProducts", products.size());
        response.put("totalCategories", categoryRepository.count());
        response.put("totalSuppliers", supplierRepository.count());
        response.put("totalStockValue", stockValue);
        response.put("lowStockCount", productService.lowStock().size());
        response.put("expiringSoonCount", productService.expiringSoon().size());
        response.put("recentProducts", productService.recentProducts());
        response.put("lowStockItems", productService.lowStock().stream().limit(6).toList());
        response.put("expiringSoonItems", productService.expiringSoon().stream().limit(6).toList());
        response.put("monthlySales", monthlySales());
        response.put("categorySales", invoiceItemRepository.categorySales());
        return response;
    }

    private List<Map<String, Object>> monthlySales() {
        List<Map<String, Object>> sales = new ArrayList<>();
        YearMonth start = YearMonth.of(2026, 1);
        YearMonth current = YearMonth.now();
        for (YearMonth ym = start; !ym.isAfter(current); ym = ym.plusMonths(1)) {
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear();
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("year", ym.getYear());
            point.put("month", ym.getMonthValue());
            point.put("label", label);
            point.put("total", invoiceService.monthTotal(ym));
            sales.add(point);
        }
        return sales;
    }

    public List<TopSoldProductResponse> topProductsByCategory(Long categoryId) {
        return invoiceItemRepository.topProductsByCategory(categoryId).stream()
                .limit(10)
                .collect(Collectors.toList());
    }
}
