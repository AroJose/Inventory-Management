package com.processedfood.inventory.repository;

import com.processedfood.inventory.model.Invoice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findTop20ByOrderByDateDesc();
    List<Invoice> findByDateGreaterThanEqualAndDateLessThan(LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"invoiceItems", "invoiceItems.product"})
    Optional<Invoice> findWithItemsById(Long id);
}
