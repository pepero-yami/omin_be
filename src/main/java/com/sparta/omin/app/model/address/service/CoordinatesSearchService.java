package com.sparta.omin.app.model.address.service;

import com.sparta.omin.app.model.address.dto.CoordinatesSearchDto;
import com.sparta.omin.app.model.address.entity.Address;
import com.sparta.omin.app.model.address.repos.AddressRepository;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoordinatesSearchService {

    private final AddressRepository addressRepository;

    public CoordinatesSearchDto getCoordinates(UUID addressId, UUID userId) {
        Address address = addressRepository.findByIdAndUserIdAndIsDeletedFalse(addressId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.ADDRESS_NOT_FOUND));
        return CoordinatesSearchDto.of(address);
    }
}
