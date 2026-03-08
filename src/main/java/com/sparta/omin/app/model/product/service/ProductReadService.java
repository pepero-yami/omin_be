package com.sparta.omin.app.model.product.service;

import com.sparta.omin.app.model.product.dto.ProductResult;
import com.sparta.omin.app.model.product.dto.ProductSummaryResult;
import com.sparta.omin.app.model.product.repos.ProductRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.List;
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
        return productRepository.findByIdAndIsDeletedFalse(productId)
            .map(ProductResult::from)
            .orElseThrow(() -> new OminBusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 메뉴의 요약 정보를 반환합니다.<br>
     * 설명을 포함하지 않는 {@link ProductSummaryResult} 를 반환합니다.<br>
     * 설명이 필요하지 않은 cart 또는 order에서 사용하세요.
     */
    public ProductSummaryResult getProductSummary(UUID productId) {
        return productRepository.findByIdAndIsDeletedFalse(productId)
            .map(ProductSummaryResult::from)
            .orElseThrow(() -> new OminBusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 매장에 등록되어있는 메뉴들의 목록을 반환합니다.<br>
     * 품절된 메뉴의 경우, UI에서 "품절되었습니다"메세지가 뜨는 것을 가정하여, 포함하여 반환합니다.
     * @param storeId
     * @return {@code List<ProductResult>}
     */
    public List<ProductResult> getProducts(UUID storeId) {
        //TODO : 존재하는 가게인지 검증하는 로직 작성
        return productRepository.findByStoreIdAndIsDeletedFalse(storeId)
            .stream()
            .map(ProductResult::from)
            .toList();
    }
}
