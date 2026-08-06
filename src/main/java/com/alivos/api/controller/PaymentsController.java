package com.alivos.api.controller;

import com.alivos.api.entity.PurchaseStatus;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.PurchaseRepository;
import com.alivos.api.service.MercadoPagoService;
import com.alivos.api.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * Mercado Pago Checkout Pro webhook. Mercado Pago calls this on every payment
 * update; we verify the signature, look up the payment, and move the matching
 * Purchase to PAID/FAILED (see MercadoPagoService and PurchaseService for the
 * rest of the flow).
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentsController {

    private static final Logger log = LoggerFactory.getLogger(PaymentsController.class);
    private static final Set<String> PAID_STATUSES = Set.of("approved");
    private static final Set<String> FAILED_STATUSES = Set.of("rejected", "cancelled");

    private final MercadoPagoService mercadoPagoService;
    private final PurchaseService purchaseService;
    private final PurchaseRepository purchaseRepository;

    @PostMapping("/mercadopago/webhook")
    public Map<String, Boolean> mercadoPagoWebhook(
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "data.id", required = false) String dataId,
            @RequestHeader(name = "x-signature", required = false) String signature,
            @RequestHeader(name = "x-request-id", required = false) String requestId
    ) {
        if (!"payment".equals(type) || dataId == null) {
            return Map.of("ok", true);
        }

        if (!mercadoPagoService.verifySignature(signature, requestId, dataId)) {
            throw ApiException.unauthorized("Firma de webhook inválida");
        }

        var payment = mercadoPagoService.getPayment(Long.valueOf(dataId));
        if (payment.externalReference() == null) {
            return Map.of("ok", true);
        }

        boolean exists = purchaseRepository.findById(payment.externalReference()).isPresent();
        if (!exists) {
            log.warn("Webhook de Mercado Pago para purchase inexistente: {}", payment.externalReference());
            return Map.of("ok", true);
        }

        if (PAID_STATUSES.contains(payment.status())) {
            purchaseService.confirmPayment(payment.externalReference(), PurchaseStatus.PAID);
        } else if (FAILED_STATUSES.contains(payment.status())) {
            purchaseService.confirmPayment(payment.externalReference(), PurchaseStatus.FAILED);
        }
        // Other statuses (pending, in_process, authorized...) are left as-is;
        // Mercado Pago will send another notification once it settles.

        return Map.of("ok", true);
    }
}
