package com.sparta.omin.app.model.address.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        UUID regionId,
        String nickname,
        String roadAddress,
        String shippingDetailAddress,
        BigDecimal addressLat,
        BigDecimal addressLong,
        boolean isDefault,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AddressResponse of(
            UUID id,
            UUID regionId,
            String nickname,
            String roadAddress,
            String shippingDetailAddress,
            BigDecimal addressLat,
            BigDecimal addressLong,
            boolean isDefault,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new AddressResponse(
                id, regionId, nickname, roadAddress, shippingDetailAddress,
                addressLat, addressLong, isDefault, createdAt, updatedAt
        );
    }
}