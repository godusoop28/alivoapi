package com.alivos.api.controller;

import com.alivos.api.dto.PurchaseDto;
import com.alivos.api.security.SecurityUtils;
import com.alivos.api.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{slug}/purchase")
@RequiredArgsConstructor
public class CoursePurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    public PurchaseDto purchase(@PathVariable String slug, Authentication authentication) {
        String userId = SecurityUtils.requireUserId(authentication);
        return purchaseService.createPurchase(userId, slug);
    }
}
