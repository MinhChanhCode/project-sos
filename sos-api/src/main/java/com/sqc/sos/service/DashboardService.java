package com.sqc.sos.service;

import com.sqc.sos.dto.dashboard.DashboardResponse;
import com.sqc.sos.model.Invoice;
import com.sqc.sos.model.Order;
import com.sqc.sos.model.OrderItem;
import com.sqc.sos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final IInvoiceRepository invoiceRepository;
    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final ITableRepository tableRepository;
    private final IReviewRepository reviewRepository;
    private final ReviewService reviewService;

    public DashboardResponse getDashboard() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        List<Invoice> paidInvoices = invoiceRepository.findAll().stream()
                .filter(i -> "PAID".equalsIgnoreCase(i.getStatus()))
                .toList();

        BigDecimal todayRevenue = paidInvoices.stream()
                .filter(i -> i.getPaidAt() != null && !i.getPaidAt().isBefore(startOfDay))
                .map(Invoice::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthRevenue = paidInvoices.stream()
                .filter(i -> i.getPaidAt() != null && !i.getPaidAt().isBefore(startOfMonth))
                .map(Invoice::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long todayOrders = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().isBefore(startOfDay))
                .count();

        long monthOrders = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().isBefore(startOfMonth))
                .count();

        long activeTables = tableRepository.findAll().stream()
                .filter(t -> Boolean.FALSE.equals(t.getIsAvailable()))
                .count();

        long pendingOrders = orderRepository.findByStatus("PENDING").size()
                + orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == null || "PENDING".equalsIgnoreCase(o.getStatus()))
                .count();

        Map<String, Long> sentimentSummary = reviewService.sentimentSummary();
        List<DashboardResponse.TopItem> topItems = buildTopItems(startOfMonth);
        List<DashboardResponse.RevenuePoint> revenueByDay = buildRevenueByDay(paidInvoices, 7);
        BigDecimal averageRating = buildAverageRating();

        return DashboardResponse.builder()
                .todayRevenue(todayRevenue)
                .monthRevenue(monthRevenue)
                .todayOrders(todayOrders)
                .monthOrders(monthOrders)
                .activeTables(activeTables)
                .pendingOrders(pendingOrders)
                .averageRating(averageRating)
                .sentimentSummary(sentimentSummary)
                .topItems(topItems)
                .revenueByDay(revenueByDay)
                .build();
    }

    private BigDecimal buildAverageRating() {
        List<com.sqc.sos.model.Review> reviews = reviewRepository.findAll().stream()
                .filter(review -> review.getRating() != null)
                .toList();
        if (reviews.isEmpty()) {
            return new BigDecimal("5.0");
        }
        double average = reviews.stream().mapToInt(com.sqc.sos.model.Review::getRating).average().orElse(5.0);
        return BigDecimal.valueOf(average).setScale(1, java.math.RoundingMode.HALF_UP);
    }

    private List<DashboardResponse.TopItem> buildTopItems(LocalDateTime from) {
        Map<Long, DashboardResponse.TopItem> map = new HashMap<>();
        for (Order order : orderRepository.findAll()) {
            if (order.getCreatedAt() == null || order.getCreatedAt().isBefore(from)) continue;
            for (OrderItem item : orderItemRepository.findByOrderId(order.getId())) {
                Long menuId = item.getMenuItem().getId();
                int qty = item.getEffectiveQuantity();
                BigDecimal rev = item.getMenuItem().getPrice().multiply(new BigDecimal(qty));
                map.merge(menuId, DashboardResponse.TopItem.builder()
                        .menuItemId(menuId)
                        .name(item.getMenuItem().getName())
                        .quantity(qty)
                        .revenue(rev)
                        .build(), (a, b) -> DashboardResponse.TopItem.builder()
                        .menuItemId(menuId)
                        .name(a.getName())
                        .quantity(a.getQuantity() + b.getQuantity())
                        .revenue(a.getRevenue().add(b.getRevenue()))
                        .build());
            }
        }
        return map.values().stream()
                .sorted(Comparator.comparing(DashboardResponse.TopItem::getQuantity).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<DashboardResponse.RevenuePoint> buildRevenueByDay(List<Invoice> invoices, int days) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<DashboardResponse.RevenuePoint> points = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();
            BigDecimal revenue = invoices.stream()
                    .filter(inv -> inv.getPaidAt() != null
                            && !inv.getPaidAt().isBefore(start)
                            && inv.getPaidAt().isBefore(end))
                    .map(Invoice::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long orders = invoices.stream()
                    .filter(inv -> inv.getPaidAt() != null
                            && !inv.getPaidAt().isBefore(start)
                            && inv.getPaidAt().isBefore(end))
                    .count();
            points.add(DashboardResponse.RevenuePoint.builder()
                    .date(day.format(fmt))
                    .revenue(revenue)
                    .orders(orders)
                    .build());
        }
        return points;
    }
}
