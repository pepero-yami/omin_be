package com.sparta.omin.app.model.region.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegionCreateRequest(
        @NotBlank
        @Size(max = 100)
        String address
) {
}