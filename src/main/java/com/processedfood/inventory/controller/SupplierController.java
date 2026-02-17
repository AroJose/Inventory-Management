package com.processedfood.inventory.controller;

import com.processedfood.inventory.model.Supplier;
import com.processedfood.inventory.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<Supplier> list() {
        return supplierService.findAll();
    }

    @PostMapping
    public Supplier create(@Valid @RequestBody Supplier supplier) {
        return supplierService.save(supplier);
    }

    @PutMapping("/{id}")
    public Supplier update(@PathVariable Long id, @Valid @RequestBody Supplier payload) {
        Supplier supplier = supplierService.findById(id);
        supplier.setName(payload.getName());
        supplier.setPhone(payload.getPhone());
        supplier.setEmail(payload.getEmail());
        supplier.setAddress(payload.getAddress());
        return supplierService.save(supplier);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        supplierService.delete(id);
    }
}
