package com.sparta.omin.app.model.product.service;

import com.sparta.omin.app.model.product.dto.ProductDetailResult;
import com.sparta.omin.app.model.product.dto.ProductWithImageResult;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.product.repos.ProductRepository;
import com.sparta.omin.app.model.store.service.StoreReadService;
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
    private final StoreReadService  storeReadService;

    /**
     * 메뉴의 상세 정보를 반환합니다.<br>
     * 메뉴별 옵션 기능은 현재 존재하지 않기에 해당 정보는 포함되지 않습니다.<br>
     * @param productId 상품의 {@code UUID}
     * @return {@link ProductDetailResult}
     */
    public ProductDetailResult getProductDetail(UUID productId) {
        return productRepository.findByIdAndIsDeletedFalse(productId)
            .map(ProductDetailResult::from)
            .orElseThrow(() -> new OminBusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 매장에 등록되어있는 메뉴들의 목록을 반환합니다.<br>
     * 품절된 메뉴의 경우, UI에서 "품절되었습니다"메세지가 뜨는 것을 가정하여, 포함하여 반환합니다.
     * @param storeId
     * @return {@code List<{@link ProductWithImageResult }>}
     */
    public List<ProductWithImageResult> getProducts(UUID storeId) {
        // 운영중인 가게인지 검증
        if(storeReadService.isStatusPending(storeId)) {
            throw new OminBusinessException(ErrorCode.BAD_REQUEST);
        }

        return productRepository.findProductListWithUrl(storeId);
    }

    public Product getProductInStore(UUID productId, UUID storeId) {
        return productRepository.findByIdAndStoreId(productId, storeId).orElseThrow(
            () -> new OminBusinessException(ErrorCode.PRODUCT_NOT_FOUND)
        );
    }
  
	public List<Product> getProductsInStore(List<UUID> productIds, UUID storeId) {
		return productRepository.findByIdInAndStoreId(productIds, storeId);	}

}
