package com.processedfood.inventory.controller;

import com.processedfood.inventory.dto.InvoiceRequest;
import com.processedfood.inventory.dto.InvoiceSummaryResponse;
import com.processedfood.inventory.model.Invoice;
import com.processedfood.inventory.service.InvoiceService;
import com.processedfood.inventory.service.PdfService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final PdfService pdfService;

    public InvoiceController(InvoiceService invoiceService, PdfService pdfService) {
        this.invoiceService = invoiceService;
        this.pdfService = pdfService;
    }

    @GetMapping
    public List<InvoiceSummaryResponse> list() {
        return invoiceService.listRecentSummaries();
    }

    @GetMapping("/{id}")
    public Invoice get(@PathVariable Long id) {
        return invoiceService.findById(id);
    }

    @PostMapping
    public Invoice create(@Valid @RequestBody InvoiceRequest request) {
        return invoiceService.create(request);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> invoicePdf(@PathVariable Long id) {
        Invoice invoice = invoiceService.findById(id);
        byte[] pdf = pdfService.invoicePdf(invoice);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + invoice.getInvoiceNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
    }
}
