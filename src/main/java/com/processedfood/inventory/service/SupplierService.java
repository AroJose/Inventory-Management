package com.processedfood.inventory.service;

import com.processedfood.inventory.model.Supplier;
import com.processedfood.inventory.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Supplier not found"));
    }

    public Supplier save(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public void delete(Long id) {
        supplierRepository.deleteById(id);
    }
}
