package com.kiladarbar.controller.customer;

import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.PartyHallPackageResponse;
import com.kiladarbar.service.PartyHallPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/party-hall/packages")
@RequiredArgsConstructor
@Tag(name = "Party Hall Packages", description = "Public party hall plan catalog")
public class PartyHallPackageController {

    private final PartyHallPackageService packageService;

    @GetMapping
    @Operation(summary = "List active party hall packages")
    public ResponseEntity<ApiResponse<List<PartyHallPackageResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(packageService.listActive()));
    }
}
