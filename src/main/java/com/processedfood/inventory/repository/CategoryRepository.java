package com.processedfood.inventory.repository;

import com.processedfood.inventory.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
