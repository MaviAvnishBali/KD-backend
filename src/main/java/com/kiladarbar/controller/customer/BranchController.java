package com.kiladarbar.controller.customer;

import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.repository.BranchRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Restaurant branches")
public class BranchController {

    private final BranchRepository branchRepository;

    @GetMapping
    @Operation(summary = "List active branches")
    public ResponseEntity<ApiResponse<List<BranchSummary>>> listBranches() {
        List<BranchSummary> branches = branchRepository.findAll().stream()
                .filter(b -> b.isActive())
                .map(b -> BranchSummary.builder()
                        .id(b.getId())
                        .name(b.getName())
                        .addressLine1(b.getAddressLine1())
                        .city(b.getCity())
                        .phone(b.getPhone())
                        .openingTime(b.getOpeningTime() != null ? b.getOpeningTime().toString() : null)
                        .closingTime(b.getClosingTime() != null ? b.getClosingTime().toString() : null)
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(branches));
    }

    @Data
    @Builder
    public static class BranchSummary {
        private UUID id;
        private String name;
        private String addressLine1;
        private String city;
        private String phone;
        private String openingTime;
        private String closingTime;
    }
}
