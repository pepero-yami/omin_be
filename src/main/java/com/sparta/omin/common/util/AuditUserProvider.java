package com.sparta.omin.common.util;

import com.sparta.omin.app.model.user.entity.User;
import java.util.UUID;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuditUserProvider {

    private AuditUserProvider() {}

    private static final UUID SYSTEM_USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 미인증/익명인 경우
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return SYSTEM_USER_ID;
        }

        Object principal = auth.getPrincipal();

        // 현재 프로젝트: principal로 User 엔티티(UserDetails 구현)가 들어오는 구조
        if (principal instanceof User user && user.getId() != null) {
            return user.getId();
        }

        return SYSTEM_USER_ID;
    }
}