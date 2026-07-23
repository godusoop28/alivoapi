package com.alivos.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Placeholder for the future Mercado Pago integration. Once real payments are
 * wired up, this should: verify the request with MERCADO_PAGO_WEBHOOK_SECRET,
 * look up the payment via the Mercado Pago API, update the matching Purchase
 * row (PAID/FAILED) and activate the Enrollment (source: PURCHASE) on PAID.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    @PostMapping("/mercadopago/webhook")
    public Map<String, Boolean> mercadoPagoWebhook() {
        return Map.of("ok", true);
    }
}
