package com.processedfood.inventory.repository;

import com.processedfood.inventory.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findTop20ByOrderByDateDesc();
    void deleteByReferenceNote(String referenceNote);
}
