package com.alivos.api.controller;

import com.alivos.api.dto.AdminPurchaseStatusRequest;
import com.alivos.api.dto.PurchaseDto;
import com.alivos.api.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/purchases")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    public Map<String, List<PurchaseDto>> listPurchases() {
        return Map.of("purchases", purchaseService.listPurchases());
    }

    @PatchMapping("/{id}/status")
    public PurchaseDto updateStatus(@PathVariable String id, @Valid @RequestBody AdminPurchaseStatusRequest request) {
        return purchaseService.confirmPayment(id, request.getStatus());
    }
}
