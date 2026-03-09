package com.sparta.omin.app.model.cart.dto.request;

import java.util.List;
import java.util.UUID;

public record CartProductDeleteRequest (
	UUID storeId,
	List<UUID> productIds
) {

}
