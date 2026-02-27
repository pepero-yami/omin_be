package com.sparta.omin.app.model.region.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class RegionResponse {

    private final UUID uuid;
    private final String address;

    public RegionResponse(UUID uuid, String address) {
        this.uuid = uuid;
        this.address = address;
    }

    public static RegionResponse of(UUID uuid, String address) {
        return new RegionResponse(uuid, address);
    }
}
