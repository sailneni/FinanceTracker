package com.financetracker.repository;

import com.financetracker.entity.Invoice;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.invoiceNumber LIKE CONCAT('ADH-', :year, '-%')")
    long countByInvoiceYear(@Param("year") int year);
    List<Invoice> findByContractorOrderByCreatedAtDesc(UserInfo contractor);
    List<Invoice> findByContractor(UserInfo contractor);
}
