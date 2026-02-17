package com.processedfood.inventory.controller;

import com.processedfood.inventory.dto.StockRequest;
import com.processedfood.inventory.model.StockTransaction;
import com.processedfood.inventory.service.StockService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/transactions")
    public List<StockTransaction> transactions() {
        return stockService.recentTransactions();
    }

    @PostMapping("/adjust")
    public StockTransaction adjust(@Valid @RequestBody StockRequest request) {
        return stockService.adjustStock(request);
    }

    @DeleteMapping("/transactions/{id}")
    public void deleteTransaction(@PathVariable Long id) {
        stockService.deleteTransaction(id);
    }
}
