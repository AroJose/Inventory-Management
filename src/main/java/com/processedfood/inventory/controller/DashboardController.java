package com.processedfood.inventory.controller;

import com.processedfood.inventory.dto.TopSoldProductResponse;
import com.processedfood.inventory.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return dashboardService.summary();
    }

    @GetMapping("/top-category-products/{categoryId}")
    public List<TopSoldProductResponse> topCategoryProducts(@PathVariable Long categoryId) {
        return dashboardService.topProductsByCategory(categoryId);
    }
}
