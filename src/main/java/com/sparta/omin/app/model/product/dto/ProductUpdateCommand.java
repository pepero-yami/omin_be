package com.sparta.omin.app.model.product.dto;

import java.util.List;

public record ProductUpdateCommand(
    String name,
    String description,
    Double price,
    List<ProductImageUpdateCommand> imageCommands
) {

}
