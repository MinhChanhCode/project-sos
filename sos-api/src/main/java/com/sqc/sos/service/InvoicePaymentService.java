package com.sqc.sos.service;

import com.sqc.sos.dto.invoice.InvoiceResponse;
import com.sqc.sos.dto.invoice.PaymentConfirmRequest;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.model.*;
import com.sqc.sos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoicePaymentService {
    private final IInvoiceRepository invoiceRepository;
    private final IPaymentRepository paymentRepository;
    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final ITableRepository tableRepository;
    private final RealtimeService realtimeService;
    private final ApplicationEventPublisher eventPublisher;

    public InvoiceResponse getByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .map(this::toResponse)
                .orElseGet(() -> createInvoice(orderId));
    }

    public List<InvoiceResponse> listAll() {
        return invoiceRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public InvoiceResponse createInvoice(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return invoiceRepository.findByOrderId(orderId)
                .map(this::toResponse)
                .orElseGet(() -> {
                    BigDecimal subtotal = calculateSubtotal(orderId);
                    BigDecimal tax = subtotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
                    BigDecimal total = subtotal.add(tax);
                    Invoice invoice = Invoice.builder()
                            .orderId(orderId)
                            .tableId(order.getTable() != null ? order.getTable().getId() : null)
                            .subtotal(subtotal)
                            .tax(tax)
                            .discount(BigDecimal.ZERO)
                            .total(total)
                            .status("PENDING")
                            .build();
                    Invoice saved = invoiceRepository.save(invoice);
                    if (order.getTable() != null) {
                        TableEntity table = order.getTable();
                        table.setTableStatus(TableStatus.WAITING_PAYMENT);
                        tableRepository.save(table);
                        eventPublisher.publishEvent(new TableStatusChangedEvent());
                    }
                    return toResponse(saved);
                });
    }

    @Transactional
    public InvoiceResponse confirmPayment(PaymentConfirmRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        Invoice invoice = invoiceRepository.findByOrderId(request.getOrderId())
                .orElseGet(() -> {
                    createInvoice(request.getOrderId());
                    return invoiceRepository.findByOrderId(request.getOrderId()).orElseThrow();
                });
        invoice.setStatus("PAID");
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        order.setStatus("PAID");
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        Payment payment = Payment.builder()
                .order(order)
                .amount(invoice.getTotal())
                .method(request.getMethod() != null ? request.getMethod() : "CASH")
                .status("COMPLETED")
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        if (order.getTable() != null) {
            TableEntity table = order.getTable();
            table.setTableStatus(TableStatus.EMPTY);
            table.setIsAvailable(true);
            tableRepository.save(table);
            eventPublisher.publishEvent(new TableStatusChangedEvent());
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "PAYMENT_COMPLETED");
        payload.put("orderId", order.getId());
        payload.put("invoiceId", invoice.getId());
        payload.put("amount", invoice.getTotal());
        realtimeService.sendPaymentEvent(payload);

        return toResponse(invoice);
    }

    private BigDecimal calculateSubtotal(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getMenuItem().getPrice()
                    .multiply(new BigDecimal(item.getEffectiveQuantity())));
        }
        return total;
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .orderId(invoice.getOrderId())
                .tableId(invoice.getTableId())
                .subtotal(invoice.getSubtotal())
                .tax(invoice.getTax())
                .discount(invoice.getDiscount())
                .total(invoice.getTotal())
                .status(invoice.getStatus())
                .createdAt(invoice.getCreatedAt())
                .paidAt(invoice.getPaidAt())
                .build();
    }
}
