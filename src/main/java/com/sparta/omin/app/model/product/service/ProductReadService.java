package com.sparta.omin.app.model.product.service;

import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.product.repos.ProductRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductReadService {

	private final ProductRepository productRepository;

	public Product getProductInStore(UUID productId, UUID storeId) {
		return productRepository.findByIdAndStoreId(productId, storeId).orElseThrow(
			() -> new OminBusinessException(ErrorCode.PRODUCT_NOT_FOUND)
		);
	}

	public List<Product> getProductsInStore(List<UUID> productIds, UUID storeId) {
		return productRepository.findByIdInAndStoreId(productIds, storeId);	}

}
