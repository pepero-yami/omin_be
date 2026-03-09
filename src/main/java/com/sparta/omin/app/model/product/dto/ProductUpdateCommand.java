package com.sparta.omin.app.model.product.dto;

import java.util.List;
import java.util.UUID;

public record ProductUpdateCommand(
    String name,
    String description,
    Double price,
    List<UUID> imgIds
) {
}
