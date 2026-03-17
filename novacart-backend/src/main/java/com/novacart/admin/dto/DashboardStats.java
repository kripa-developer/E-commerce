package com.novacart.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStats(
        long totalUsers,
        long totalProducts,
        long totalOrders,
        long activeOrders,
        BigDecimal revenueToday,
        BigDecimal revenueThisMonth,
        List<RecentOrderDto> recentOrders
) {}
