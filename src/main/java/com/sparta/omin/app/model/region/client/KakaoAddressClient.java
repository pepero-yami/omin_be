package com.sparta.omin.app.model.region.client;

import com.sparta.omin.app.model.region.client.dto.KakaoAddressSearchResponse;
import com.sparta.omin.common.error.KakaoApiException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class KakaoAddressClient {

    private final RestTemplate restTemplate;

    @Value("${kakao.rest-api-key}")
    private String restApiKey;

    public KakaoAddressClient(@Qualifier("kakaoRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public record KakaoAddressResult(
            String depth3Address,
            BigDecimal latitude,
            BigDecimal longitude
    ) {}

    public KakaoAddressResult searchAddress(String query) {
        KakaoAddressSearchResponse.Document first = requestFirstDocument(query);

        if (first.getAddress() == null) {
            throw new KakaoApiException("카카오 주소 검색 결과가 없습니다.");
        }

        String depth3 = buildDepth3(first.getAddress());
        BigDecimal longitude = parseBigDecimal(first.getX(), "longitude(x)");
        BigDecimal latitude = parseBigDecimal(first.getY(), "latitude(y)");

        return new KakaoAddressResult(depth3, latitude, longitude);
    }

    //Region이 사용 중인 기존 메서드
    public String normalizeToRegionDepth3(String query) {
        KakaoAddressSearchResponse.Document first = requestFirstDocument(query);

        if (first.getAddress() == null) {
            throw new KakaoApiException("카카오 주소 검색 결과가 없습니다.");
        }

        return buildDepth3(first.getAddress());
    }

    private KakaoAddressSearchResponse.Document requestFirstDocument(String query) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("주소(query)가 비어있습니다.");
        }

        URI uri = UriComponentsBuilder
                .fromUriString("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query", query)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + restApiKey);
        headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<KakaoAddressSearchResponse> response;
        try {
            response = restTemplate.exchange(uri, HttpMethod.GET, entity, KakaoAddressSearchResponse.class);
        } catch (RestClientException e) {
            throw new KakaoApiException("카카오 주소 검색 API 호출에 실패했습니다.", e);
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new KakaoApiException("카카오 주소 검색 API 응답이 올바르지 않습니다.");
        }

        KakaoAddressSearchResponse body = response.getBody();
        KakaoAddressSearchResponse.Document first = body.firstDocumentOrNull();
        if (first == null) {
            throw new KakaoApiException("카카오 주소 검색 결과가 없습니다.");
        }
        return first;
    }

    private String buildDepth3(KakaoAddressSearchResponse.Address addr) {
        String depth1 = trimToNull(addr.getRegion1depthName());
        String depth2 = trimToNull(addr.getRegion2depthName());
        String depth3 = trimToNull(StringUtils.hasText(addr.getRegion3depthName())
                ? addr.getRegion3depthName()
                : addr.getRegion3depthHName());

        // depth1, depth3은 필수로 간주
        if (!StringUtils.hasText(depth1) || !StringUtils.hasText(depth3)) {
            throw new KakaoApiException("유효하지 않은 주소(address)입니다.");
        }

        List<String> parts = new ArrayList<>();
        parts.add(depth1);
        if (StringUtils.hasText(depth2)) parts.add(depth2);
        parts.add(depth3);

        return String.join(" ", parts);
    }

    private String trimToNull(String s) {
        if (!StringUtils.hasText(s)) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private BigDecimal parseBigDecimal(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new KakaoApiException("카카오 주소 검색 결과에 " + fieldName + " 값이 없습니다.");
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new KakaoApiException("카카오 주소 검색 결과의 " + fieldName + " 값이 올바르지 않습니다.");
        }
    }
}