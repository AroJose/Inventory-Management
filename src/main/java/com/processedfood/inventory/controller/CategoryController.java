package com.processedfood.inventory.controller;

import com.processedfood.inventory.model.Category;
import com.processedfood.inventory.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Category> list() {
        return categoryService.findAll();
    }

    @PostMapping
    public Category create(@Valid @RequestBody Category category) {
        return categoryService.save(category);
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @Valid @RequestBody Category payload) {
        Category category = categoryService.findById(id);
        category.setName(payload.getName());
        category.setDescription(payload.getDescription());
        return categoryService.save(category);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
