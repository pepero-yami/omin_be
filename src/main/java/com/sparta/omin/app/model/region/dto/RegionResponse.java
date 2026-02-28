package com.sparta.omin.app.model.region.dto;

import java.util.UUID;

public record RegionResponse(
        UUID uuid,
        String address
) {
    public static RegionResponse of(UUID uuid, String address) {
        return new RegionResponse(uuid, address);
    }
}