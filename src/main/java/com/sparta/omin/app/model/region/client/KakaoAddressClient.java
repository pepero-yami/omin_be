package com.sparta.omin.app.model.region.client;

import com.sparta.omin.app.model.region.client.dto.KakaoAddressSearchResponse;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
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
            String roadAddress,     // 정규화된 도로명 주소 - 중간의 공백2번 등 오타 거르기 위해
            BigDecimal latitude,
            BigDecimal longitude
    ) {}

    public KakaoAddressResult searchAddress(String query) {
        KakaoAddressSearchResponse.Document first = requestFirstDocument(query);

        if (first.getAddress() == null) {
            throw new ApiException(ErrorCode.KAKAO_NO_RESULT);
        }

        // DB 조회용 - 법정동
        String depth3 = buildDepth3(first.getAddress());

        // 카카오가 정제해준 표준 도로명 주소 (공백 등이 정리된 상태로 돌아옴!) - 도로명주소 있으면 도로명주소, 아니면 지번주소
        String normalizedRoadAddress = (first.getRoadAddress() != null)
                ? first.getRoadAddress().getAddressName()
                : first.getAddress().getAddressName();

        BigDecimal longitude = parseBigDecimal(first.getX(), "longitude(x)");
        BigDecimal latitude = parseBigDecimal(first.getY(), "latitude(y)");

        return new KakaoAddressResult(depth3, normalizedRoadAddress, latitude, longitude);
    }

    //Region이 사용 중인 기존 메서드 - 주소 문자열만 반환
    public String normalizeToRegionDepth3(String query) {
        KakaoAddressSearchResponse.Document first = requestFirstDocument(query);

        if (first.getAddress() == null) {
            throw new ApiException(ErrorCode.KAKAO_NO_RESULT);
        }

        return buildDepth3(first.getAddress());
    }

    private KakaoAddressSearchResponse.Document requestFirstDocument(String query) {
        if (!StringUtils.hasText(query)) {
            throw new ApiException(ErrorCode.KAKAO_EMPTY_QUERY);
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
            throw new ApiException(ErrorCode.KAKAO_API_CALL_FAILED);
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new ApiException(ErrorCode.KAKAO_INVALID_RESPONSE);
        }

        KakaoAddressSearchResponse body = response.getBody();
        KakaoAddressSearchResponse.Document first = body.firstDocumentOrNull();
        if (first == null) {
            throw new ApiException(ErrorCode.KAKAO_NO_RESULT);
        }
        return first;
    }

    //법정동 우선, 비었다면 행정동(HName)
    private String buildDepth3(KakaoAddressSearchResponse.Address addr) {
        String depth1 = trimToNull(addr.getRegion1depthName());
        String depth2 = trimToNull(addr.getRegion2depthName());
        String depth3 = trimToNull(StringUtils.hasText(addr.getRegion3depthName())
                ? addr.getRegion3depthName()
                : addr.getRegion3depthHName());

        // depth1, depth3은 필수로 간주
        if (!StringUtils.hasText(depth1) || !StringUtils.hasText(depth3)) {
            throw new ApiException(ErrorCode.REGION_INVALID_ADDRESS);
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
            // x(경도)와 y(위도) 구분에 따른 에러 코드 매핑
            throw new ApiException(fieldName.contains("x")
                    ? ErrorCode.KAKAO_NO_LONGITUDE
                    : ErrorCode.KAKAO_NO_LATITUDE);
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorCode.KAKAO_INVALID_COORDINATE);
        }
    }
}