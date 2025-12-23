package com.financetracker.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber; // "INV-2025-001"
    private String clientName;
    private String clientEmail;
    private String clientAddress;

    private BigDecimal hourlyRate;
    private BigDecimal hoursWorked;
    private BigDecimal subtotal;
    private BigDecimal hstAmount; // 13%
    private BigDecimal totalAmount;

    private LocalDate invoiceDate = LocalDate.now();
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @ManyToOne UserInfo contractor;

    // Company details (stored once per contractor)
    private String companyName = "Ailneni DevHub Inc.";
    private String companyAddress = "267 Chine Drive, Scarborough, ON M1M 2L6";
    private String companyPhone = "(519) 992-7610";
    private String hstNumber = "759171366 RT 0001";

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDate periodStart;
    private LocalDate periodEnd;

    @ElementCollection
    @CollectionTable(name = "invoice_timesheet", joinColumns = @JoinColumn(name = "invoice_id"))
    private List<TimesheetEntry> timesheetEntries;

    public Invoice() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(BigDecimal hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getHstAmount() {
        return hstAmount;
    }

    public void setHstAmount(BigDecimal hstAmount) {
        this.hstAmount = hstAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public UserInfo getContractor() {
        return contractor;
    }

    public void setContractor(UserInfo contractor) {
        this.contractor = contractor;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getHstNumber() {
        return hstNumber;
    }

    public void setHstNumber(String hstNumber) {
        this.hstNumber = hstNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public List<TimesheetEntry> getTimesheetEntries() {
        return timesheetEntries;
    }

    public void setTimesheetEntries(List<TimesheetEntry> timesheetEntries) {
        this.timesheetEntries = timesheetEntries;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    @PrePersist
    @PreUpdate
    private void calculateFromTimesheet() {
        if (timesheetEntries != null && !timesheetEntries.isEmpty()) {
            // Sum hours from timesheet
            BigDecimal totalHours = timesheetEntries.stream()
                    .map(TimesheetEntry::getHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            this.hoursWorked = totalHours;
            if (hourlyRate != null) {
                this.subtotal = totalHours.multiply(hourlyRate);
                this.hstAmount = this.subtotal.multiply(BigDecimal.valueOf(0.13));
                this.totalAmount = this.subtotal.add(this.hstAmount);
            }

            if (periodStart != null && periodEnd == null) {
                this.periodEnd = periodStart.plusDays(13); // 2 weeks
            }
            if (dueDate == null) {
                this.dueDate = LocalDate.now().plusDays(30);
            }
        }
    }
}

