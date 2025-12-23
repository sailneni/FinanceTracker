package com.financetracker.service;

import com.financetracker.entity.Invoice;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PdfGenerator {

    private static final float[] COLUMN_WIDTHS = {3, 1, 1, 1.5f, 1.5f};

    public byte[] generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(40, 40, 40, 40);

        // Add sections
        addCompanyHeader(document, invoice);
        addInvoiceInfo(document, invoice);
        addClientInfo(document, invoice);
        addLineItemsTable(document, invoice);
        addTotalsSection(document, invoice);
        addFooter(document, invoice);

        document.close();
        return baos.toByteArray();
    }

    // Company header with branding
    private void addCompanyHeader(Document document, Invoice invoice) {
        Table headerTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
        headerTable.setMarginBottom(20);

        Cell logoCell = new Cell();
        logoCell.add(new Paragraph("Your Company Ltd.")
                .setFontSize(28).setBold().setTextAlignment(TextAlignment.LEFT));
        logoCell.add(new Paragraph(invoice.getCompanyAddress())
                .setFontSize(11).setTextAlignment(TextAlignment.LEFT));
        logoCell.add(new Paragraph(invoice.getCompanyPhone())
                .setFontSize(11).setTextAlignment(TextAlignment.LEFT));
        logoCell.setBorder(Border.NO_BORDER);

        Cell hstCell = new Cell();
        hstCell.add(new Paragraph("HST Registration")
                .setFontSize(12).setBold().setTextAlignment(TextAlignment.RIGHT));
        hstCell.add(new Paragraph(invoice.getHstNumber())
                .setFontSize(11).setTextAlignment(TextAlignment.RIGHT));
        hstCell.setBorder(Border.NO_BORDER);

        headerTable.addCell(logoCell);
        headerTable.addCell(hstCell);

        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }

    // Invoice number, date, due date
    private void addInvoiceInfo(Document document, Invoice invoice) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth();
        infoTable.setMarginBottom(20);

        // Set header background
        infoTable.setBackgroundColor(ColorConstants.LIGHT_GRAY);

        addCell(infoTable, "Invoice #", invoice.getInvoiceNumber(), true);
        addCell(infoTable, "Invoice Date", invoice.getInvoiceDate().toString(), true);
        addCell(infoTable, "Due Date",
                invoice.getDueDate() != null ? invoice.getDueDate().toString() : "TBD", true);

        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }

    private void addCell(Table table, String label, String value, boolean bold) {
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label).setBold().setFontSize(12));
        labelCell.setPadding(8);
        table.addCell(labelCell);

        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value).setFontSize(12));
        valueCell.setPadding(8);
        table.addCell(valueCell);
    }

    // Client details
    private void addClientInfo(Document document, Invoice invoice) {
        Table clientTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();
        clientTable.setMarginBottom(20);

        // Seller info
        Cell sellerCell = new Cell();
        sellerCell.add(new Paragraph("FROM:")
                .setFontSize(12).setBold());
        sellerCell.add(new Paragraph(invoice.getContractor().getName())
                .setFontSize(11));
        sellerCell.add(new Paragraph(invoice.getCompanyAddress())
                .setFontSize(11));
        sellerCell.setBorder(Border.NO_BORDER);

        // Bill to
        Cell billToCell = new Cell();
        billToCell.add(new Paragraph("BILL TO:")
                .setFontSize(12).setBold());
        billToCell.add(new Paragraph(invoice.getClientName())
                .setFontSize(11));
        billToCell.add(new Paragraph(invoice.getClientEmail())
                .setFontSize(11));
        billToCell.setBorder(Border.NO_BORDER);

        clientTable.addCell(sellerCell);
        clientTable.addCell(billToCell);

        document.add(clientTable);
        document.add(new Paragraph("\n"));
    }

    // Line items table
    private void addLineItemsTable(Document document, Invoice invoice) {
        Table itemsTable = new Table(COLUMN_WIDTHS).useAllAvailableWidth();
        itemsTable.setMarginBottom(20);

        // Header row
        String[] headers = {"Description", "Rate", "Hours", "Amount", "Tax"};
        for (String header : headers) {
            Cell cell = new Cell();
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.add(new Paragraph(header).setBold().setFontSize(11));
            cell.setPadding(10);
            cell.setTextAlignment(TextAlignment.CENTER);
            itemsTable.addHeaderCell(cell);
        }

        // Service line
        itemsTable.addCell(createCell("Professional Services"));
        itemsTable.addCell(createCell("$" + formatCurrency(invoice.getHourlyRate()), TextAlignment.RIGHT));
        itemsTable.addCell(createCell(invoice.getHoursWorked().toString(), TextAlignment.RIGHT));
        itemsTable.addCell(createCell("$" + formatCurrency(invoice.getSubtotal()), TextAlignment.RIGHT));
        itemsTable.addCell(createCell("$" + formatCurrency(invoice.getHstAmount()), TextAlignment.RIGHT));

        document.add(itemsTable);
    }

    // Totals summary
    private void addTotalsSection(Document document, Invoice invoice) {
        Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1.5f, 1.5f}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        // Empty cells for alignment
        totalsTable.addCell(createCell("", Border.NO_BORDER));

        // Subtotal row
        Cell subtotalLabel = createCell("Subtotal:", Border.NO_BORDER);
        subtotalLabel.setTextAlignment(TextAlignment.RIGHT).setBold();
        totalsTable.addCell(subtotalLabel);
        totalsTable.addCell(createCell("$" + formatCurrency(invoice.getSubtotal()), Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT).setBold());

        // HST row
        totalsTable.addCell(createCell("", Border.NO_BORDER));
        Cell hstLabel = createCell("HST (13%):", Border.NO_BORDER);
        hstLabel.setTextAlignment(TextAlignment.RIGHT).setBold();
        totalsTable.addCell(hstLabel);
        totalsTable.addCell(createCell("$" + formatCurrency(invoice.getHstAmount()), Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT).setBold());

        // Total row (highlighted)
        totalsTable.addCell(createCell("", Border.NO_BORDER));
        Cell totalLabel = createCell("TOTAL:", Border.NO_BORDER);
        totalLabel.setBackgroundColor(ColorConstants.YELLOW)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBold()
                .setFontSize(14);
        totalsTable.addCell(totalLabel);

        Cell totalAmount = createCell("$" + formatCurrency(invoice.getTotalAmount()), Border.NO_BORDER);
        totalAmount.setBackgroundColor(ColorConstants.YELLOW)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBold()
                .setFontSize(14);
        totalsTable.addCell(totalAmount);

        document.add(totalsTable);
    }

    // Footer with payment terms
    private void addFooter(Document document, Invoice invoice) {
        document.add(new Paragraph("\n"));

        Table footerTable = new Table(1).useAllAvailableWidth();
        footerTable.setMarginTop(20);
        footerTable.setBorderTop(new SolidBorder(1));

        Cell footerCell = new Cell();
        footerCell.add(new Paragraph("Payment Terms:")
                .setBold().setFontSize(11));
        footerCell.add(new Paragraph("Due within 30 days of invoice date. Please include invoice number with payment.")
                .setFontSize(10));
        footerCell.add(new Paragraph("Thank you for your business!")
                .setItalic().setFontSize(10).setMarginTop(10));
        footerCell.setBorder(Border.NO_BORDER);

        footerTable.addCell(footerCell);
        document.add(footerTable);
    }

    // Helper methods
    private Cell createCell(String content) {
        return createCell(content, Border.NO_BORDER);
    }

    private Cell createCell(String content, Border border) {
        Cell cell = new Cell();
        cell.add(new Paragraph(content).setFontSize(11));
        cell.setPadding(8);
        cell.setBorder(border);
        return cell;
    }

    private Cell createCell(String content, TextAlignment alignment) {
        Cell cell = createCell(content);
        cell.setTextAlignment(alignment);
        return cell;
    }

    private String formatCurrency(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }
}
