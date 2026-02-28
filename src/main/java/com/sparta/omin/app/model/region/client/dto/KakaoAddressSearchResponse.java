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

        private Address address;

        @JsonProperty("address_type")
        private String addressType;

        @JsonProperty("address_name")
        private String addressName;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {

        @JsonProperty("region_1depth_name")
        private String region1depthName;

        @JsonProperty("region_2depth_name")
        private String region2depthName;

        @JsonProperty("region_3depth_h_name")
        private String region3depthHName;

        @JsonProperty("region_3depth_name")
        private String region3depthName;
    }
}
