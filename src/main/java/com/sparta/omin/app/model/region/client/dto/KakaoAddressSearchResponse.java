package com.sparta.omin.app.model.region.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoAddressSearchResponse {

    private List<Document> documents;

    public Document firstDocumentOrNull() {
        if (documents == null || documents.isEmpty()) return null;
        return documents.get(0);
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {

        @JsonProperty("address") //지번 주소 정보 (동 단위 조회용)
        private Address address;

        @JsonProperty("address_type")
        private String addressType;

        @JsonProperty("address_name") // 지번 혹은 도로명 전체 명칭
        private String addressName;

        @JsonProperty("road_address") // 도로명 주소 객체 매핑
        private RoadAddress roadAddress;

        private String x; //경도
        private String y; //위도
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        @JsonProperty("address_name") // 지번 주소 정제 명칭
        private String addressName;

        @JsonProperty("region_1depth_name")
        private String region1depthName;

        @JsonProperty("region_2depth_name")
        private String region2depthName;

        @JsonProperty("region_3depth_h_name")
        private String region3depthHName;

        @JsonProperty("region_3depth_name")
        private String region3depthName;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoadAddress {
        @JsonProperty("address_name") // 정제된 도로명 주소
        private String addressName;

        @JsonProperty("region_1depth_name")
        private String region1depthName;

        @JsonProperty("region_2depth_name")
        private String region2depthName;

        @JsonProperty("region_3depth_name")
        private String region3depthName;

        @JsonProperty("road_name")
        private String roadName;
    }
}