package com.kiladarbar.service;

import com.kiladarbar.dto.response.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCustomerService {
    Page<CustomerResponse> listCustomers(String tier, Pageable pageable);
}
