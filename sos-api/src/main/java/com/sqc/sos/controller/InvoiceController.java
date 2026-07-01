package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.invoice.InvoiceResponse;
import com.sqc.sos.dto.invoice.PaymentConfirmRequest;
import com.sqc.sos.service.InvoicePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoicePaymentService invoicePaymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(invoicePaymentService.listAll(), "Lấy hóa đơn thành công"));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(invoicePaymentService.getByOrderId(orderId), "Lấy hóa đơn thành công"));
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(invoicePaymentService.createInvoice(orderId), "Tạo hóa đơn thành công"));
    }

    @PostMapping("/public/session/{sessionId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createForSession(@PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.success(invoicePaymentService.createInvoiceForSession(sessionId), "Tạo hóa đơn thanh toán thành công"));
    }

    @PostMapping("/payment/confirm")
    public ResponseEntity<ApiResponse<InvoiceResponse>> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        return ResponseEntity.ok(ApiResponse.success(invoicePaymentService.confirmPayment(request), "Xác nhận thanh toán thành công"));
    }

    @GetMapping("/order/{orderId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long orderId) {
        InvoiceResponse invoice = invoicePaymentService.getByOrderId(orderId);
        String content = "HOA DON\nOrder: " + orderId + "\nTotal: " + invoice.getTotal() + " VND\nStatus: " + invoice.getStatus();
        byte[] bytes = content.getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + orderId + ".txt")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
