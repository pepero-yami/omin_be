package com.sparta.omin.common.audit;

import com.sparta.omin.common.util.AuditUserProvider;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component
public class SecurityAuditorAware implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return Optional.of(AuditUserProvider.currentUserId());
    }
}