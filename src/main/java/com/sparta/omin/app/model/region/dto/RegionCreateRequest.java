package com.sparta.omin.app.model.region.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegionCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String address;

}