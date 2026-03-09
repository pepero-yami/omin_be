package com.sparta.omin.app.model.stats.service;

import com.sparta.omin.app.model.stats.entity.StoreRatingStat;
import com.sparta.omin.app.model.stats.repos.StoreRatingStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreRatingStatService {

    private final StoreRatingStatRepository storeRatingStatRepository;

    public Optional<StoreRatingStat> getStat(UUID storeId) {
        return storeRatingStatRepository.findByStoreId(storeId);
    }
}
