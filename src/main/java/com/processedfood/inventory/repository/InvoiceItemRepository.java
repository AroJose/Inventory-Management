package com.processedfood.inventory.repository;

import com.processedfood.inventory.dto.CategorySalesResponse;
import com.processedfood.inventory.dto.TopSoldProductResponse;
import com.processedfood.inventory.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    @Query("""
            select new com.processedfood.inventory.dto.CategorySalesResponse(
                c.id,
                c.name,
                sum(ii.quantity)
            )
            from InvoiceItem ii
            join ii.product p
            join p.category c
            group by c.id, c.name
            order by sum(ii.quantity) desc
            """)
    List<CategorySalesResponse> categorySales();

    @Query("""
            select new com.processedfood.inventory.dto.TopSoldProductResponse(
                p.id,
                p.name,
                p.imagePath,
                sum(ii.quantity)
            )
            from InvoiceItem ii
            join ii.product p
            where p.category.id = :categoryId
            group by p.id, p.name, p.imagePath
            order by sum(ii.quantity) desc
            """)
    List<TopSoldProductResponse> topProductsByCategory(@Param("categoryId") Long categoryId);
}
