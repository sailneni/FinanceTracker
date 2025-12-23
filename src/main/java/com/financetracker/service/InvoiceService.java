package com.financetracker.service;

import com.financetracker.dto.InvoiceRequest;
import com.financetracker.dto.InvoiceResponse;
import com.financetracker.dto.TimesheetEntryDto;
import com.financetracker.entity.Invoice;
import com.financetracker.entity.TimesheetEntry;
import com.financetracker.entity.UserInfo;
import com.financetracker.repository.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    @Autowired
    private InvoiceRepository repo;
    @Autowired
    private UserService userService;
    @Autowired
    private InvoicePdfGenerator pdfGenerator;

    private static final BigDecimal HST_RATE = new BigDecimal("0.13");

    public InvoiceResponse createInvoice(InvoiceRequest req) {
        UserInfo contractor = userService.getCurrentUser();

        Invoice invoice = new Invoice();
        invoice.setClientName(req.getClientName());
        invoice.setClientEmail(req.getClientEmail());
        invoice.setClientAddress(req.getClientAddress());
        invoice.setHourlyRate(req.getHourlyRate());
        invoice.setContractor(contractor);
        invoice.setDueDate(req.getDueDate());

        // TIMESHEET SUPPORT
        if (req.getTimesheet() != null && !req.getTimesheet().isEmpty()) {
            List<TimesheetEntry> timesheetEntries = req.getTimesheet().stream()
                    .map(this::toTimesheetEntry)
                    .collect(Collectors.toList());
            invoice.setTimesheetEntries(timesheetEntries);
            invoice.setPeriodStart(req.getPeriodStart());
            invoice.setPeriodEnd(req.getPeriodEnd());

        } else {
            // FALLBACK: Manual hours input
            invoice.setHoursWorked(req.getHoursWorked());
            BigDecimal subtotal = req.getHourlyRate().multiply(req.getHoursWorked());
            invoice.setSubtotal(subtotal);
            invoice.setHstAmount(subtotal.multiply(HST_RATE));
            invoice.setTotalAmount(subtotal.add(invoice.getHstAmount()));
        }

        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber();
        invoice.setInvoiceNumber(invoiceNumber);

        Invoice saved = repo.save(invoice);  // Triggers @PrePersist
        return toResponse(saved);
    }

    private String generateInvoiceNumber() {
        int year = LocalDate.now().getYear();
        long count = repo.countByInvoiceYear(year);
        return String.format("ADH-%d-%04d", year, count + 1);
    }

    public List<InvoiceResponse> getAllInvoices(){
        UserInfo user = userService.getCurrentUser();
        return repo.findByContractorOrderByCreatedAtDesc(user)
                .stream().map(this::toResponse).toList();
    }

    public byte[] downloadPDF(Long id) throws IOException {
        Invoice invoice = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + id));

        return pdfGenerator.generateInvoicePdf(invoice);
    }

    private TimesheetEntry toTimesheetEntry(TimesheetEntryDto dto) {
        TimesheetEntry entry = new TimesheetEntry();
        entry.setWeekStart(dto.getWeekStart());
        entry.setWeekEnd(dto.getWeekEnd());
        entry.setHours(dto.getHours());
        return entry;
    }

    private InvoiceResponse toResponse(Invoice inv) {
        InvoiceResponse resp = new InvoiceResponse();
        resp.setInvoiceNumber(inv.getInvoiceNumber());
        resp.setClientName(inv.getClientName());
        resp.setClientEmail(inv.getClientEmail());
        resp.setHourlyRate(inv.getHourlyRate());
        resp.setHoursWorked(inv.getHoursWorked());
        resp.setSubtotal(inv.getSubtotal());
        resp.setHstAmount(inv.getHstAmount());
        resp.setTotalAmount(inv.getTotalAmount());
        resp.setInvoiceDate(inv.getInvoiceDate());
        resp.setDueDate(inv.getDueDate());
        resp.setStatus(inv.getStatus().toString());
        resp.setId(inv.getId());

        if (inv.getTimesheetEntries() != null && !inv.getTimesheetEntries().isEmpty()) {
            resp.setPeriodStart(inv.getPeriodStart());
            resp.setPeriodEnd(inv.getPeriodEnd());
            resp.setTimesheet(inv.getTimesheetEntries().stream()
                    .map(this::toTimesheetDto)
                    .collect(Collectors.toList()));
        }


        return resp;
    }

    private TimesheetEntryDto toTimesheetDto(TimesheetEntry entity) {
        TimesheetEntryDto dto = new TimesheetEntryDto();
        dto.setWeekStart(entity.getWeekStart());
        dto.setWeekEnd(entity.getWeekEnd());
        dto.setHours(entity.getHours());
        return dto;
    }
}
