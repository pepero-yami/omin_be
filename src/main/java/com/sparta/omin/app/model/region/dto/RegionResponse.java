package com.sparta.omin.app.model.region.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RegionResponse {

    private final UUID uuid;
    private final String address;

    public static RegionResponse of(UUID uuid, String address) {
        return new RegionResponse(uuid, address);
    }
}
