package com.financetracker.service;

import com.financetracker.entity.Invoice;
import com.financetracker.entity.TimesheetEntry;
import com.financetracker.entity.UserInfo;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

@Component
public class InvoicePdfGenerator {

    public byte[] generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(50, 50, 50, 50);

        // 1. Header with invoice title
        addHeader(doc, invoice);

        // 2. Two-column: Company Info | Bill To
        addCompanyAndClientSection(doc, invoice);

        // 3. Timesheet breakdown table (with actual timesheet data)
        addTimesheetTable(doc, invoice);

        // 4. Summary totals with HST
        addTotalsSection(doc, invoice);

        doc.close();
        return baos.toByteArray();
    }

    private void addHeader(Document doc, Invoice invoice) {
        // Main title
        doc.add(new Paragraph("INVOICE")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // Date | Invoice # row
        Table headerRow = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
        headerRow.setMarginBottom(30);

        headerRow.addCell(new Cell()
                .add(new Paragraph("Date: " + formatDate(invoice.getInvoiceDate())))
                .setBorder(Border.NO_BORDER)
                .setFontSize(12));

        headerRow.addCell(new Cell()
                .add(new Paragraph("Invoice #: " + invoice.getInvoiceNumber()))
                .setBorder(Border.NO_BORDER)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.RIGHT));

        doc.add(headerRow);
    }

    private void addCompanyAndClientSection(Document doc, Invoice invoice) {
        Table twoCol = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
        twoCol.setMarginBottom(30);

        // COMPANY INFORMATION (Left)
        Cell companyCell = new Cell();
        companyCell.setBorder(Border.NO_BORDER);
        companyCell.add(new Paragraph("COMPANY INFORMATION")
                .setFontSize(14).setBold());
        companyCell.add(new Paragraph(invoice.getCompanyName())
                .setFontSize(12).setBold());
        companyCell.add(new Paragraph(safeGet(invoice.getCompanyAddress(), obj -> obj))
                .setFontSize(11));
        companyCell.add(new Paragraph("HST: " + invoice.getHstNumber())
                .setFontSize(11));
        companyCell.add(new Paragraph("Contractor Name: " +
                safeGet(invoice.getContractor(), user -> ((UserInfo) user).getName()))
                .setFontSize(11));
        twoCol.addCell(companyCell);

        // BILLED TO (Right)
        Cell billToCell = new Cell();
        billToCell.setBorder(Border.NO_BORDER);
        billToCell.add(new Paragraph("Billed To")
                .setFontSize(14).setBold());
        billToCell.add(new Paragraph(invoice.getClientName())
                .setFontSize(12).setBold());
        billToCell.add(new Paragraph(invoice.getClientEmail())
                .setFontSize(11));
        billToCell.add(new Paragraph(invoice.getClientAddress())
                .setFontSize(11));
        twoCol.addCell(billToCell);

        doc.add(twoCol);
    }

    // ✅ UPDATED: Now handles actual timesheet entries
    private void addTimesheetTable(Document doc, Invoice invoice) {
        // Description
        doc.add(new Paragraph("For services rendered at CIBC")
                .setItalic().setFontSize(11).setMarginBottom(20));

        Table timesheet = new Table(new float[]{2, 1, 1, 1.5F}).useAllAvailableWidth();
        timesheet.setMarginBottom(20);

        // Headers
        timesheet.addHeaderCell(createHeaderCell("Timesheet"));
        timesheet.addHeaderCell(createHeaderCell("Hours"));
        timesheet.addHeaderCell(createHeaderCell("Rate/Hr"));
        timesheet.addHeaderCell(createHeaderCell("Amount"));

        BigDecimal totalAmount = BigDecimal.ZERO;

        // ✅ Use actual timesheet entries if available
        if (invoice.getTimesheetEntries() != null && !invoice.getTimesheetEntries().isEmpty()) {
            for (TimesheetEntry entry : invoice.getTimesheetEntries()) {
                String period = formatDateRange(entry.getWeekStart(), entry.getWeekEnd());
                BigDecimal weekAmount = entry.getHours().multiply(invoice.getHourlyRate());

                timesheet.addCell(createCell(period));
                timesheet.addCell(createCell(formatDecimal(entry.getHours())));
                timesheet.addCell(createCell("$" + formatCurrency(invoice.getHourlyRate())));
                timesheet.addCell(createCell("$" + formatCurrency(weekAmount))
                        .setTextAlignment(TextAlignment.RIGHT));

                totalAmount = totalAmount.add(weekAmount);
            }
        } else {
            // ✅ FALLBACK: Use simple hours if no timesheet
            timesheet.addCell(createCell("Services Rendered"));
            timesheet.addCell(createCell(formatDecimal(invoice.getHoursWorked())));
            timesheet.addCell(createCell("$" + formatCurrency(invoice.getHourlyRate())));
            totalAmount = invoice.getSubtotal();
            timesheet.addCell(createCell("$" + formatCurrency(totalAmount))
                    .setTextAlignment(TextAlignment.RIGHT));
        }

        doc.add(timesheet);

    }

    private void addTotalsSection(Document doc, Invoice invoice) {
        Table totals = new Table(new float[]{3, 1}).useAllAvailableWidth();
        totals.setMarginBottom(20);

        // Subtotal
        totals.addCell(createCell("Subtotal").setBold());
        totals.addCell(createCell("$" + formatCurrency(invoice.getSubtotal()))
                .setTextAlignment(TextAlignment.RIGHT).setBold());

        // HST rate and amount
        totals.addCell(createCell("H.S.T. (13%)").setBold());
        totals.addCell(createCell("$" + formatCurrency(invoice.getHstAmount()))
                .setTextAlignment(TextAlignment.RIGHT).setBold());

        // INVOICE TOTAL (Highlighted)
        Cell totalLabel = createCell("INVOICE TOTAL").setBold().setFontSize(16);
        totalLabel.setBackgroundColor(new DeviceRgb(230, 230, 230));
        totals.addCell(totalLabel);

        Cell totalAmount = createCell("$" + formatCurrency(invoice.getTotalAmount()))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBold()
                .setFontSize(18);
        totalAmount.setBackgroundColor(new DeviceRgb(230, 230, 230));
        totals.addCell(totalAmount);

        // Due date
//        totals.addCell(new Cell()
//                .add(new Paragraph("Due Date: " + formatDate(invoice.getDueDate()))
//                        .setFontSize(11))
//                .setBorder(Border.NO_BORDER));
//        totals.addCell(new Cell().setBorder(Border.NO_BORDER));

        doc.add(totals);
    }

    // ✅ Helper: Format date range (e.g., "Dec 07 - Dec 13")
    private String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        return start.format(formatter) + " - " + end.format(formatter);
    }

    // ✅ Helper: Format decimal numbers
    private String formatDecimal(BigDecimal value) {
        if (value == null) return "0.00";
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private Cell createHeaderCell(String text) {
        Cell cell = new Cell().add(new Paragraph(text).setBold().setFontSize(12));
        cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        cell.setPadding(12);
        cell.setTextAlignment(TextAlignment.CENTER);
        return cell;
    }

    private Cell createCell(String text) {
        Cell cell = new Cell().add(new Paragraph(text).setFontSize(11));
        cell.setPadding(8);
        return cell;
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0.00";
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) : "N/A";
    }

    private String safeGet(Object obj, Function function) {
        return obj != null ? function.apply(obj).toString() : "N/A";
    }
}