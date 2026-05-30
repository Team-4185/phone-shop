package com.challengeteam.shop.service.admin;

import com.challengeteam.shop.dto.admin.customer.AdminCustomerDetailsResponseDto;
import com.challengeteam.shop.dto.admin.customer.AdminCustomerFilterDto;
import com.challengeteam.shop.dto.admin.customer.AdminCustomerKpiResponseDto;
import com.challengeteam.shop.dto.admin.customer.AdminCustomerListItemResponseDto;
import org.springframework.data.domain.Page;

/** Read operations used by the admin Customers API. */
public interface AdminCustomerService {

  Page<AdminCustomerListItemResponseDto> getCustomers(
      int page, int size, AdminCustomerFilterDto filterDto);

  AdminCustomerKpiResponseDto getCustomerKpi();

  AdminCustomerDetailsResponseDto getCustomerById(Long id);
}
