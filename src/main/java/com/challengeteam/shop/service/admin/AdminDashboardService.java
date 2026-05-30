package com.challengeteam.shop.service.admin;

import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardLowStockAlertResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardRecentOrderResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSalesByBrandResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSalesPointResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSalesPeriod;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSummaryResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardTopProductResponseDto;
import java.util.List;

/** Read operations used by the admin Dashboard API. */
public interface AdminDashboardService {

  AdminDashboardSummaryResponseDto getSummary();

  List<AdminDashboardSalesPointResponseDto> getSalesAnalytics(AdminDashboardSalesPeriod period);

  List<AdminDashboardSalesByBrandResponseDto> getSalesByBrand();

  List<AdminDashboardTopProductResponseDto> getTopSellingProducts();

  List<AdminDashboardRecentOrderResponseDto> getRecentOrders();

  List<AdminDashboardLowStockAlertResponseDto> getLowStockAlerts();
}
