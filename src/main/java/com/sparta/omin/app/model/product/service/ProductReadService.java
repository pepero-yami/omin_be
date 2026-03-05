package com.sparta.omin.app.model.product.service;

import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.product.repository.ProductRepository;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductReadService {

	private final ProductRepository productRepository;

	public Product getProductById(UUID productId) {
		return productRepository.findById(productId).orElseThrow(
			() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND)
		);
	}

}
