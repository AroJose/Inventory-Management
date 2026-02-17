package com.processedfood.inventory.repository;

import com.processedfood.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByQuantityLessThan(Integer quantity);
    List<Product> findByExpiryDateLessThanEqual(LocalDate date);
    Optional<Product> findByBarcode(String barcode);
    boolean existsByBarcodeAndIdNot(String barcode, Long id);
}
