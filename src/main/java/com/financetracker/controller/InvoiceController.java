package com.financetracker.controller;

import com.financetracker.dto.InvoiceRequest;
import com.financetracker.dto.InvoiceResponse;
import com.financetracker.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/create")
    public ResponseEntity<InvoiceResponse> createInvoice(@RequestBody @Valid InvoiceRequest req) {
        return ResponseEntity.ok(invoiceService.createInvoice(req));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAll() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPDF(@PathVariable Long id) throws IOException {
        invoiceService.downloadPDF(id);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition",
                        "attachment; filename=\"Invoice" + ".pdf\"")
                .body( invoiceService.downloadPDF(id));
    }

//    @PutMapping("/{id}/status")
//    public ResponseEntity<InvoiceResponse> updateStatus(@PathVariable Long id,
//                                                        @RequestBody UpdateStatusRequest req) {
//        return ResponseEntity.ok(invoiceService.updateStatus(id, req.status()));
//    }
}
