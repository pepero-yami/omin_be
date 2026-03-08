package com.sparta.omin.app.model.region.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoAddressSearchResponse(
        List<Document> documents
) {
    public Document firstDocumentOrNull() {
        if (documents == null || documents.isEmpty()) return null;
        return documents.get(0);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(
            Address address,
            @JsonProperty("address_type") String addressType,
            @JsonProperty("address_name") String addressName,
            @JsonProperty("road_address") RoadAddress roadAddress,
            String x, // 경도
            String y  // 위도
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Address(
            @JsonProperty("address_name") String addressName,
            @JsonProperty("region_1depth_name") String region1depthName,
            @JsonProperty("region_2depth_name") String region2depthName,
            @JsonProperty("region_3depth_h_name") String region3depthHName,
            @JsonProperty("region_3depth_name") String region3depthName
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RoadAddress(
            @JsonProperty("address_name") String addressName,
            @JsonProperty("region_1depth_name") String region1depthName,
            @JsonProperty("region_2depth_name") String region2depthName,
            @JsonProperty("region_3depth_name") String region3depthName,
            @JsonProperty("road_name") String roadName
    ) {}
}