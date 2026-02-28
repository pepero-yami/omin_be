package com.sparta.omin.app.model.region.client;

import com.sparta.omin.app.model.region.client.dto.KakaoAddressSearchResponse;
import com.sparta.omin.common.error.KakaoApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    /**
     * 카카오 주소 검색 API를 이용해 입력 주소(query)를 정규화한다.
     * 규칙:
     * - region_1depth_name, region_2depth_name, region_3depth_(h_)name 를 이용
     * - region_3depth_h_name이 있으면 우선 사용, 없으면 region_3depth_name 사용
     * - 중간 depth가 ""(빈 값)인 경우는 건너뛰기 (공백이 2번 생기지 않게)
     * - 여러 documents가 있을 경우 맨 처음 결과 사용
     * - 카카오 호출 실패/응답 이상/검색 결과 없음이면 예외 발생(=등록 실패)
     */
    public String normalizeToRegionDepth3(String query) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("주소(query)가 비어있습니다.");
        }

        URI uri = UriComponentsBuilder
                .fromUriString("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query", query)
                // 한글/공백 등이 들어가므로 반드시 인코딩
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
        if (first == null || first.getAddress() == null) {
            throw new KakaoApiException("카카오 주소 검색 결과가 없습니다.");
        }

        KakaoAddressSearchResponse.Address addr = first.getAddress();

        String depth1 = trimToNull(addr.getRegion1depthName());
        String depth2 = trimToNull(addr.getRegion2depthName());
        String depth3 = trimToNull(StringUtils.hasText(addr.getRegion3depthHName())
                ? addr.getRegion3depthHName()
                : addr.getRegion3depthName());

        // depth1, depth3은 필수로 간주 (depth2는 특별시/광역시 케이스 등에서 비어 있을 수 있음)
        if (!StringUtils.hasText(depth1) || !StringUtils.hasText(depth3)) {
            throw new KakaoApiException("카카오 주소 검색 결과에서 region depth 정보를 추출할 수 없습니다.");
        }

        List<String> parts = new ArrayList<>(3);
        if (StringUtils.hasText(depth1)) parts.add(depth1);
        if (StringUtils.hasText(depth2)) parts.add(depth2);
        if (StringUtils.hasText(depth3)) parts.add(depth3);

        return String.join(" ", parts);
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}