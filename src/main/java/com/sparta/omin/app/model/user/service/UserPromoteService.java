package com.sparta.omin.app.model.user.service;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.app.model.user.constants.Role;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.repository.UserRepository;

import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPromoteService {

    private final UserRepository userRepository;

    @Transactional
    public void promoteToOwnerIfCustomer(UUID userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole() == Role.CUSTOMER) {
            user.promoteToOwner();
            log.info("유저 권한 변경 - userId: {}, CUSTOMER -> OWNER", userId);
        }
    }
}
