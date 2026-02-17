package com.processedfood.inventory.service;

import com.processedfood.inventory.dto.StockRequest;
import com.processedfood.inventory.exception.BadRequestException;
import com.processedfood.inventory.model.Product;
import com.processedfood.inventory.model.StockTransaction;
import com.processedfood.inventory.model.StockType;
import com.processedfood.inventory.repository.ProductRepository;
import com.processedfood.inventory.repository.StockTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class StockService {
    private final ProductRepository productRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public StockService(ProductRepository productRepository, StockTransactionRepository stockTransactionRepository) {
        this.productRepository = productRepository;
        this.stockTransactionRepository = stockTransactionRepository;
    }

    public List<StockTransaction> recentTransactions() {
        return stockTransactionRepository.findTop20ByOrderByDateDesc();
    }

    @Transactional
    public StockTransaction adjustStock(StockRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        int current = product.getQuantity() == null ? 0 : product.getQuantity();
        int qty = request.getQuantity();
        if (request.getType() == StockType.OUT && current < qty) {
            throw new BadRequestException("Insufficient stock for stock out");
        }

        product.setQuantity(request.getType() == StockType.IN ? current + qty : current - qty);
        productRepository.save(product);

        StockTransaction tx = new StockTransaction();
        tx.setProduct(product);
        tx.setType(request.getType());
        tx.setQuantity(qty);
        tx.setReferenceNote(request.getReferenceNote());
        return stockTransactionRepository.save(tx);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        StockTransaction tx = stockTransactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Stock transaction not found"));
        Product product = tx.getProduct();
        int current = product.getQuantity() == null ? 0 : product.getQuantity();
        int qty = tx.getQuantity();

        if (tx.getType() == StockType.IN) {
            if (current < qty) {
                throw new BadRequestException("Cannot delete IN transaction because current stock is lower than transaction quantity");
            }
            product.setQuantity(current - qty);
        } else {
            product.setQuantity(current + qty);
        }

        productRepository.save(product);
        stockTransactionRepository.delete(tx);
    }
}
