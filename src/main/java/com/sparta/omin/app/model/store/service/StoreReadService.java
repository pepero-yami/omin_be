package com.sparta.omin.app.model.store.service;

import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.repos.StoreRepository;
import com.sparta.omin.app.model.user.service.UserReadService;
import com.sparta.omin.common.error.constants.ErrorCode;
import com.sparta.omin.common.error.exceptions.CommonException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreReadService {

    private final StoreRepository storeRepository;
    private final UserReadService userReadService;

    /**
     * 자신이 소유한 Store인지 검증합니다.
     */
    public Boolean isOwnedStore(UUID storeId, String userEmail) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new CommonException(ErrorCode.STORE_NOT_FOUND));
        UUID requesterId = userReadService.getUserInfo(userEmail).id();

        return requesterId.equals(store.getOwnerId());
    }

    /**
     * StoreId만 보유한 프록시 객체 반환
     */
    public Store getStoreReference(UUID storeId) {
        return storeRepository.getReferenceById(storeId);
    }
}
