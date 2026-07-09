package com.kiladarbar.controller.admin;

import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.CustomerResponse;
import com.kiladarbar.service.AdminCustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'OWNER', 'SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin · Customers", description = "Customer directory with order & loyalty stats")
public class AdminCustomerController {

    private final AdminCustomerService customerService;

    @GetMapping
    @Operation(summary = "List customers with order & loyalty stats")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> list(
            @RequestParam(required = false) String tier,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(customerService.listCustomers(tier, pageable)));
    }
}
