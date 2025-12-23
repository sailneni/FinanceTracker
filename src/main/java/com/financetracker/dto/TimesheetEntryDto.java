package com.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TimesheetEntryDto {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private BigDecimal hours;

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(LocalDate weekEnd) {
        this.weekEnd = weekEnd;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }
}
