package com.alivos.api.service;

import com.alivos.api.exception.ApiException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
public class UploadService {

    @Value("${app.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${app.cloudinary.api-key:}")
    private String apiKey;

    @Value("${app.cloudinary.api-secret:}")
    private String apiSecret;

    private Cloudinary cloudinary;

    private synchronized Cloudinary getCloudinary() {
        if (cloudName == null || cloudName.isBlank() || apiKey == null || apiKey.isBlank()
                || apiSecret == null || apiSecret.isBlank()) {
            throw ApiException.badRequest(
                    "Cloudinary no está configurado. Configura CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY y CLOUDINARY_API_SECRET.");
        }
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
        }
        return cloudinary;
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Selecciona un archivo para subir.");
        }
        try {
            Map<?, ?> result = getCloudinary().uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "auto",
                    "folder", "alivos"
            ));
            Object url = result.get("secure_url");
            if (url == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Cloudinary no devolvió una URL válida.");
            }
            return url.toString();
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Error subiendo archivo a Cloudinary: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo subir el archivo. Intenta de nuevo.");
        }
    }
}
