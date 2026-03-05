package com.sparta.omin.app.model.product.Service;

import com.sparta.omin.app.model.product.dto.ProductResult;
import com.sparta.omin.app.model.product.dto.ProductSummaryResult;
import com.sparta.omin.app.model.product.repos.ProductRepository;
import com.sparta.omin.common.error.constants.ErrorCode;
import com.sparta.omin.common.error.exceptions.CommonException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductReadService {

    private final ProductRepository productRepository;

    /**
     * 메뉴의 상세 정보를 반환합니다.<br>
     * 메뉴별 옵션 기능은 현재 존재하지 않기에 해당 정보는 포함되지 않습니다.<br>
     * {@link ProductResult} 를 반환합니다.
     */
    public ProductResult getProduct(UUID productId) {
        return productRepository.findById(productId)
            .map(ProductResult::from)
            .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 메뉴의 요약 정보를 반환합니다.<br>
     * 설명을 포함하지 않는 {@link ProductSummaryResult} 를 반환합니다.<br>
     * 설명이 필요하지 않은 cart 또는 order에서 사용하세요.
     */
    public ProductSummaryResult getProductSummary(UUID productId) {
        return productRepository.findById(productId)
            .map(ProductSummaryResult::from)
            .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
