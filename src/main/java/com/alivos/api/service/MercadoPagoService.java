package com.alivos.api.service;

import com.alivos.api.exception.ApiException;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

/**
 * Thin wrapper around the Mercado Pago Checkout Pro APIs: creates the payment
 * preference a user gets redirected to, resolves a payment id back to its
 * status, and verifies the webhook signature. Real production credentials are
 * used here (no sandbox) — see the project plan for the testing boundary this
 * implies (we never submit a real card on the client's behalf).
 */
@Service
public class MercadoPagoService {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoService.class);

    private final String accessToken;
    private final String webhookSecret;
    private final String frontendUrl;

    public MercadoPagoService(
            @Value("${app.mercadopago.access-token}") String accessToken,
            @Value("${app.mercadopago.webhook-secret}") String webhookSecret,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.accessToken = accessToken;
        this.webhookSecret = webhookSecret;
        this.frontendUrl = frontendUrl;
    }

    @PostConstruct
    void init() {
        if (accessToken != null && !accessToken.isBlank()) {
            MercadoPagoConfig.setAccessToken(accessToken);
        } else {
            log.warn("MERCADO_PAGO_ACCESS_TOKEN no está configurado; los pagos con Mercado Pago fallarán.");
        }
    }

    public boolean isConfigured() {
        return accessToken != null && !accessToken.isBlank();
    }

    public record PreferenceResult(String preferenceId, String initPoint) {
    }

    public PreferenceResult createPreference(String purchaseId, String title, Integer amount) {
        if (!isConfigured()) {
            throw ApiException.badRequest("Los pagos con Mercado Pago no están configurados todavía");
        }
        try {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(title)
                    .quantity(1)
                    .currencyId("MXN")
                    .unitPrice(BigDecimal.valueOf(amount))
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(frontendUrl + "/?payment=success")
                    .pending(frontendUrl + "/?payment=pending")
                    .failure(frontendUrl + "/?payment=failure")
                    .build();

            PreferenceRequest.PreferenceRequestBuilder builder = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(backUrls)
                    .externalReference(purchaseId);
            // auto_return requires an https success URL — Mercado Pago rejects
            // it outright otherwise, which only comes up in local dev (http).
            if (frontendUrl.startsWith("https")) {
                builder.autoReturn("approved");
            }

            Preference preference = new PreferenceClient().create(builder.build());
            return new PreferenceResult(preference.getId(), preference.getInitPoint());
        } catch (MPApiException apiEx) {
            String body = apiEx.getApiResponse() != null ? apiEx.getApiResponse().getContent() : "sin detalle";
            log.error("Error creando preferencia de Mercado Pago para purchase {}: {}", purchaseId, body, apiEx);
            throw ApiException.badRequest("No se pudo generar el pago con Mercado Pago");
        } catch (MPException ex) {
            log.error("Error creando preferencia de Mercado Pago para purchase {}", purchaseId, ex);
            throw ApiException.badRequest("No se pudo generar el pago con Mercado Pago");
        }
    }

    public record PaymentResult(String status, String externalReference) {
    }

    public PaymentResult getPayment(Long paymentId) {
        try {
            Payment payment = new PaymentClient().get(paymentId);
            return new PaymentResult(payment.getStatus(), payment.getExternalReference());
        } catch (MPApiException | MPException ex) {
            log.error("Error consultando el pago {} en Mercado Pago", paymentId, ex);
            throw ApiException.badRequest("No se pudo consultar el pago en Mercado Pago");
        }
    }

    /**
     * Verifies the `x-signature` header per Mercado Pago's webhook spec
     * (manifest = "id:{dataId};request-id:{xRequestId};ts:{ts};", HMAC-SHA256
     * with the per-integration webhook secret). If no secret has been
     * configured yet in Render, verification is skipped with a warning so the
     * webhook keeps working while the client finishes that setup — once the
     * secret is set this becomes a hard requirement.
     */
    public boolean verifySignature(String xSignature, String xRequestId, String dataId) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("MERCADO_PAGO_WEBHOOK_SECRET no está configurado; se omite la verificación de firma.");
            return true;
        }
        if (xSignature == null || xRequestId == null || dataId == null) {
            return false;
        }

        String ts = null;
        String v1 = null;
        for (String part : xSignature.split(",")) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim();
            if ("ts".equals(key)) ts = kv[1].trim();
            if ("v1".equals(key)) v1 = kv[1].trim();
        }
        if (ts == null || v1 == null) {
            return false;
        }

        String manifest = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);
            return MessageDigest.isEqual(
                    computed.getBytes(StandardCharsets.UTF_8),
                    v1.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception ex) {
            log.error("Error verificando la firma del webhook de Mercado Pago", ex);
            return false;
        }
    }
}
