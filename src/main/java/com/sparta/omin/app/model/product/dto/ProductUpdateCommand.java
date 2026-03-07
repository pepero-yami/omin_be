package com.sparta.omin.app.model.product.dto;

public record ProductUpdateCommand(
    String name,
    String description,
    Double price
) {
}
