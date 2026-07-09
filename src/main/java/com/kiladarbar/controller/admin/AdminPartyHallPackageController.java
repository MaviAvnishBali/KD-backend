package com.kiladarbar.controller.admin;

import com.kiladarbar.dto.request.PartyHallPackageRequest;
import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.PartyHallPackageResponse;
import com.kiladarbar.service.PartyHallPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/party-hall/packages")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'OWNER', 'SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin · Party Hall Packages", description = "Manage the party hall plan catalog")
public class AdminPartyHallPackageController {

    private final PartyHallPackageService packageService;

    @GetMapping
    @Operation(summary = "List all packages (incl. inactive)")
    public ResponseEntity<ApiResponse<List<PartyHallPackageResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(packageService.adminList()));
    }

    @PostMapping
    @Operation(summary = "Create a package")
    public ResponseEntity<ApiResponse<PartyHallPackageResponse>> create(@Valid @RequestBody PartyHallPackageRequest req) {
        return ResponseEntity.ok(ApiResponse.success(packageService.create(req), "Package created"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a package")
    public ResponseEntity<ApiResponse<PartyHallPackageResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody PartyHallPackageRequest req) {
        return ResponseEntity.ok(ApiResponse.success(packageService.update(id, req)));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle a package's active state")
    public ResponseEntity<ApiResponse<PartyHallPackageResponse>> toggle(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(packageService.toggle(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a package")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        packageService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Package deleted"));
    }
}
