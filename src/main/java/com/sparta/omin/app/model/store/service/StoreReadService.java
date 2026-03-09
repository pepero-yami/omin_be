package com.sparta.omin.app.model.store.service;

import com.sparta.omin.app.model.store.code.Status;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.repos.StoreRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreReadService {

    private final StoreRepository storeRepository;

    /**
     * 자신이 소유한 Store인지 검증합니다.
     */
    public Boolean isOwnedStore(UUID storeId, UUID userId) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new OminBusinessException(ErrorCode.STORE_NOT_FOUND));
        return userId.equals(store.getOwnerId());
    }

    /**
     * StoreId만 보유한 프록시 객체 반환
     */
    public Store getStoreReference(UUID storeId) {
        return storeRepository.getReferenceById(storeId);
    }

    public Boolean isStatusPending(UUID storeId) {
        Store store = storeRepository.findByIdAndIsDeletedFalse(storeId).orElseThrow(() -> new OminBusinessException(ErrorCode.STORE_NOT_FOUND));
        return store.getStatus() == Status.PENDING;
    }
}
