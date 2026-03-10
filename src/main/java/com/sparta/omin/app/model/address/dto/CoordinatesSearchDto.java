package com.sparta.omin.app.model.address.dto;

import com.sparta.omin.app.model.address.entity.Address;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CoordinatesSearchDto(
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static CoordinatesSearchDto of(Address address) {
        return CoordinatesSearchDto.builder()
                .latitude(address.getAddressLat())
                .longitude(address.getAddressLong())
                .build();
    }
}
