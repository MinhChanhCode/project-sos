package com.sqc.sos.dto.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardResponse {
    BigDecimal todayRevenue;
    BigDecimal monthRevenue;
    long todayOrders;
    long monthOrders;
    long activeTables;
    long pendingOrders;
    Map<String, Long> sentimentSummary;
    List<TopItem> topItems;
    List<RevenuePoint> revenueByDay;

    @Data
    @Builder
    public static class TopItem {
        Long menuItemId;
        String name;
        long quantity;
        BigDecimal revenue;
    }

    @Data
    @Builder
    public static class RevenuePoint {
        String date;
        BigDecimal revenue;
        long orders;
    }
}
