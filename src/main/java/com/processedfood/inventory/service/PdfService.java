package com.processedfood.inventory.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.processedfood.inventory.model.Invoice;
import com.processedfood.inventory.model.InvoiceItem;
import com.processedfood.inventory.model.Product;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {
    private static final DecimalFormat PRICE = new DecimalFormat("#,##0.00");

    public byte[] invoicePdf(Invoice invoice) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter.getInstance(document, out);
            document.open();

            addTitle(document, "Processed Food Inventory - Invoice");
            addMeta(document, "Invoice No: " + invoice.getInvoiceNumber());
            addMeta(document, "Date: " + invoice.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            addMeta(document, "Customer: " + invoice.getCustomerName());
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3.5f, 1.5f, 1f, 1.5f, 1.5f});
            header(table, "Product");
            header(table, "Barcode");
            header(table, "Qty");
            header(table, "Price");
            header(table, "Subtotal");

            for (InvoiceItem item : invoice.getInvoiceItems()) {
                row(table, item.getProduct().getName());
                row(table, item.getProduct().getBarcode());
                row(table, String.valueOf(item.getQuantity()));
                row(table, PRICE.format(item.getPrice()));
                row(table, PRICE.format(item.getSubtotal()));
            }

            document.add(table);
            document.add(Chunk.NEWLINE);
            addMeta(document, "Total: " + PRICE.format(invoice.getTotalAmount()));
            addMeta(document, "Thank you for your business.");
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate invoice PDF");
        }
    }

    public byte[] productsPdf(List<Product> products) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 30, 30, 20, 20);
            PdfWriter.getInstance(document, out);
            document.open();
            addTitle(document, "Processed Food Inventory - Product Export");
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.2f, 2f, 1.5f, 1.2f, 1.3f, 1.3f, 1.6f});
            header(table, "Name");
            header(table, "Category");
            header(table, "Barcode");
            header(table, "Qty");
            header(table, "Buy");
            header(table, "Sell");
            header(table, "Expiry");

            for (Product product : products) {
                row(table, product.getName());
                row(table, product.getCategory() != null ? product.getCategory().getName() : "");
                row(table, product.getBarcode());
                row(table, String.valueOf(product.getQuantity()));
                row(table, PRICE.format(product.getPurchasePrice()));
                row(table, PRICE.format(product.getSellingPrice()));
                row(table, String.valueOf(product.getExpiryDate()));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate products PDF");
        }
    }

    private void addTitle(Document document, String title) throws DocumentException {
        Font font = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(46, 125, 50));
        Paragraph paragraph = new Paragraph(title, font);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        document.add(paragraph);
    }

    private void addMeta(Document document, String text) throws DocumentException {
        Font font = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(70, 70, 70));
        document.add(new Paragraph(text, font));
    }

    private void header(PdfPTable table, String text) {
        Font font = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(46, 125, 50));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void row(PdfPTable table, String text) {
        Font font = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(40, 40, 40));
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        table.addCell(cell);
    }
}
