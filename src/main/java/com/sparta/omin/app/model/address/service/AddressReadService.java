package com.sparta.omin.app.model.address.service;

import com.sparta.omin.app.model.address.entity.Address;
import com.sparta.omin.app.model.address.repos.AddressRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressReadService {

    private final AddressRepository addressRepository;

    public Address getMyAddress(UUID userId, UUID addressId) {
        return addressRepository.findByIdAndUserIdAndIsDeletedFalse(addressId, userId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ADDRESS_NOT_FOUND));
    }
}
