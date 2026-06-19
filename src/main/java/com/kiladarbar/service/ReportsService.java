package com.kiladarbar.service;

import java.time.LocalDate;
import java.util.UUID;

public interface ReportsService {
    Object getDashboardStats(UUID branchId);
    Object getSalesReport(UUID branchId, String period, LocalDate from, LocalDate to);
    Object getTopSellingItems(UUID branchId, int limit, LocalDate from, LocalDate to);
    Object getCustomerReport(UUID branchId, String period);
    Object getDeliveryReport(UUID branchId, String period);
    Object getStaffReport(UUID branchId, String period);
    Object getProfitAnalysis(UUID branchId, String period);
    byte[] exportSalesReport(UUID branchId, String period, String format, LocalDate from, LocalDate to);
}
