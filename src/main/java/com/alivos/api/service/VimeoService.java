package com.alivos.api.service;

import com.alivos.api.dto.VimeoResolvedDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class VimeoService {

    private static final String FALLBACK_THUMBNAIL =
            "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=crop&w=800&q=80";

    private static final Pattern[] VIMEO_ID_PATTERNS = {
            Pattern.compile("vimeo\\.com/(?:channels/[\\w-]+/|groups/[\\w-]+/videos/|album/\\d+/video/|video/)?(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("player\\.vimeo\\.com/video/(\\d+)", Pattern.CASE_INSENSITIVE),
    };

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.vimeo.access-token:}")
    private String vimeoAccessToken;

    public String extractVimeoId(String url) {
        for (Pattern pattern : VIMEO_ID_PATTERNS) {
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    public String buildEmbedUrl(String vimeoId) {
        return "https://player.vimeo.com/video/" + vimeoId;
    }

    public VimeoResolvedDto resolve(String url) {
        String vimeoId = extractVimeoId(url);
        if (vimeoId == null) {
            return new VimeoResolvedDto(null, null, null, null, null);
        }

        String embedUrl = buildEmbedUrl(vimeoId);

        VimeoResolvedDto viaPrivateApi = fetchViaPrivateApi(vimeoId, embedUrl);
        if (viaPrivateApi != null) {
            return viaPrivateApi;
        }

        VimeoResolvedDto viaOEmbed = fetchOEmbed(url, vimeoId, embedUrl);
        if (viaOEmbed != null) {
            return viaOEmbed;
        }

        return new VimeoResolvedDto(vimeoId, null, FALLBACK_THUMBNAIL, null, embedUrl);
    }

    private VimeoResolvedDto fetchViaPrivateApi(String vimeoId, String embedUrl) {
        if (vimeoAccessToken == null || vimeoAccessToken.isBlank()) {
            return null;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.vimeo.com/videos/" + vimeoId))
                    .header("Authorization", "bearer " + vimeoAccessToken)
                    .header("Accept", "application/vnd.vimeo.*+json;version=3.4")
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return null;
            }
            JsonNode json = objectMapper.readTree(response.body());
            String title = json.hasNonNull("name") ? json.get("name").asText() : null;
            Integer durationSeconds = json.hasNonNull("duration") ? json.get("duration").asInt() : null;
            String thumbnail = FALLBACK_THUMBNAIL;
            JsonNode sizes = json.path("pictures").path("sizes");
            if (sizes.isArray() && sizes.size() > 0) {
                thumbnail = sizes.get(sizes.size() - 1).path("link").asText(FALLBACK_THUMBNAIL);
            }
            Integer durationMinutes = durationSeconds != null ? Math.round(durationSeconds / 60f) : null;
            return new VimeoResolvedDto(vimeoId, title, thumbnail, durationMinutes, embedUrl);
        } catch (Exception ex) {
            log.warn("No se pudo resolver metadata de Vimeo vía API privada: {}", ex.getMessage());
            return null;
        }
    }

    private VimeoResolvedDto fetchOEmbed(String url, String vimeoId, String embedUrl) {
        try {
            String oembedUrl = "https://vimeo.com/api/oembed.json?url=" + URLEncoder.encode(url, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(oembedUrl))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return null;
            }
            JsonNode json = objectMapper.readTree(response.body());
            String title = json.hasNonNull("title") ? json.get("title").asText() : null;
            String thumbnail = json.hasNonNull("thumbnail_url") ? json.get("thumbnail_url").asText() : FALLBACK_THUMBNAIL;
            Integer durationSeconds = json.hasNonNull("duration") ? json.get("duration").asInt() : null;
            Integer durationMinutes = durationSeconds != null ? Math.round(durationSeconds / 60f) : null;
            return new VimeoResolvedDto(vimeoId, title, thumbnail, durationMinutes, embedUrl);
        } catch (Exception ex) {
            log.warn("No se pudo resolver metadata de Vimeo vía oEmbed: {}", ex.getMessage());
            return null;
        }
    }
}
