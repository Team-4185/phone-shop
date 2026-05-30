package com.challengeteam.shop.service.admin;

import com.challengeteam.shop.dto.admin.AdminSidebarCountersResponseDto;

/** Provides counters for admin sidebar navigation badges. */
public interface AdminSidebarCountersService {

  AdminSidebarCountersResponseDto getSidebarCounters();
}
