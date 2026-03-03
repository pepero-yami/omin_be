package com.sparta.omin.app.model.ai.repos;

import com.sparta.omin.app.model.ai.entity.AiLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiLogRepository extends JpaRepository<AiLog, UUID> {
}
