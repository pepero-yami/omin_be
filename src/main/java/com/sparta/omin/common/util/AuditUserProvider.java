package com.sparta.omin.common.util;

import java.util.UUID;

public final class AuditUserProvider {

    private AuditUserProvider() {}

    /**
     * TODO(auth): 유저/인증 작업 완료 후, SecurityContext(또는 @AuthenticationPrincipal)에서
     *             현재 로그인 사용자의 UUID를 반환하도록 변경.
     *             이 파일만 수정하면 created_by/updated_by/deleted_by 세팅 로직은 그대로 유지 가능.
     */
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static UUID currentUserId() {
        return SYSTEM_USER_ID;
    }
}