package com.processedfood.inventory.service;

import com.processedfood.inventory.exception.BadRequestException;
import com.processedfood.inventory.model.Product;
import com.processedfood.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductService {
    private static final String PLACEHOLDER = "/img/placeholder.svg";

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    public ProductService(ProductRepository productRepository, FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> recentProducts() {
        return productRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(6)
                .toList();
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
    }

    public Product findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode).orElseThrow(() -> new NoSuchElementException("Product not found for barcode"));
    }

    public Product save(Product product, MultipartFile imageFile) {
        validateProduct(product, null);
        String imagePath = fileStorageService.storeProductImage(product.getName(), imageFile);
        if (imagePath != null) {
            product.setImagePath(imagePath);
        } else if (product.getImagePath() == null || product.getImagePath().isBlank()) {
            product.setImagePath(PLACEHOLDER);
        }
        return productRepository.save(product);
    }

    public Product update(Long id, Product payload, MultipartFile imageFile) {
        Product existing = findById(id);
        validateProduct(payload, id);

        existing.setName(payload.getName());
        existing.setDescription(payload.getDescription());
        existing.setCategory(payload.getCategory());
        existing.setBrand(payload.getBrand());
        existing.setBatchNumber(payload.getBatchNumber());
        existing.setBarcode(payload.getBarcode());
        existing.setExpiryDate(payload.getExpiryDate());
        existing.setPurchasePrice(payload.getPurchasePrice());
        existing.setSellingPrice(payload.getSellingPrice());
        existing.setQuantity(payload.getQuantity());
        existing.setSupplier(payload.getSupplier());

        String imagePath = fileStorageService.storeProductImage(payload.getName(), imageFile);
        if (imagePath != null) {
            existing.setImagePath(imagePath);
        } else if (existing.getImagePath() == null || existing.getImagePath().isBlank()) {
            existing.setImagePath(PLACEHOLDER);
        }

        return productRepository.save(existing);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> lowStock() {
        return productRepository.findByQuantityLessThan(10);
    }

    public List<Product> expiringSoon() {
        return productRepository.findByExpiryDateLessThanEqual(LocalDate.now().plusDays(7));
    }

    private void validateProduct(Product product, Long productId) {
        if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Expiry date cannot be in past");
        }
        if (product.getQuantity() != null && product.getQuantity() < 0) {
            throw new BadRequestException("Quantity cannot be negative");
        }
        if (product.getPurchasePrice() != null && product.getSellingPrice() != null
                && product.getSellingPrice().compareTo(product.getPurchasePrice()) < 0) {
            throw new BadRequestException("Selling price cannot be less than purchase price");
        }
        if (product.getBarcode() == null || product.getBarcode().isBlank()) {
            throw new BadRequestException("Barcode is required");
        }
        if (productId == null) {
            if (productRepository.findByBarcode(product.getBarcode()).isPresent()) {
                throw new BadRequestException("Barcode must be unique");
            }
        } else if (productRepository.existsByBarcodeAndIdNot(product.getBarcode(), productId)) {
            throw new BadRequestException("Barcode must be unique");
        }
    }
}
