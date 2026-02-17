package com.processedfood.inventory.controller;

import com.processedfood.inventory.model.Product;
import com.processedfood.inventory.service.CategoryService;
import com.processedfood.inventory.service.PdfService;
import com.processedfood.inventory.service.ProductService;
import com.processedfood.inventory.service.SupplierService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    private final PdfService pdfService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             SupplierService supplierService,
                             PdfService pdfService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
        this.pdfService = pdfService;
    }

    @GetMapping
    public List<Product> list() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping("/barcode/{barcode}")
    public Product byBarcode(@PathVariable String barcode) {
        return productService.findByBarcode(barcode);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        byte[] pdf = pdfService.productsPdf(productService.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/low-stock")
    public List<Product> lowStock() {
        return productService.lowStock();
    }

    @GetMapping("/expiring-soon")
    public List<Product> expiringSoon() {
        return productService.expiringSoon();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Product create(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String batchNumber,
            @RequestParam String barcode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @RequestParam BigDecimal purchasePrice,
            @RequestParam BigDecimal sellingPrice,
            @RequestParam Integer quantity,
            @RequestParam Long supplierId,
            @RequestParam(required = false) MultipartFile imageFile
    ) {
        Product product = buildProduct(name, description, categoryId, brand, batchNumber, barcode, expiryDate,
                purchasePrice, sellingPrice, quantity, supplierId);
        return productService.save(product, imageFile);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Product update(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String batchNumber,
            @RequestParam String barcode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @RequestParam BigDecimal purchasePrice,
            @RequestParam BigDecimal sellingPrice,
            @RequestParam Integer quantity,
            @RequestParam Long supplierId,
            @RequestParam(required = false) MultipartFile imageFile
    ) {
        Product product = buildProduct(name, description, categoryId, brand, batchNumber, barcode, expiryDate,
                purchasePrice, sellingPrice, quantity, supplierId);
        return productService.update(id, product, imageFile);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    private Product buildProduct(String name,
                                 String description,
                                 Long categoryId,
                                 String brand,
                                 String batchNumber,
                                 String barcode,
                                 LocalDate expiryDate,
                                 BigDecimal purchasePrice,
                                 BigDecimal sellingPrice,
                                 Integer quantity,
                                 Long supplierId) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setCategory(categoryService.findById(categoryId));
        product.setBrand(brand);
        product.setBatchNumber(batchNumber);
        product.setBarcode(barcode);
        product.setExpiryDate(expiryDate);
        product.setPurchasePrice(purchasePrice);
        product.setSellingPrice(sellingPrice);
        product.setQuantity(quantity);
        product.setSupplier(supplierService.findById(supplierId));
        return product;
    }
}
