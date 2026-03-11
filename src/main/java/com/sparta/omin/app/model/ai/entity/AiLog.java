package com.sparta.omin.app.model.ai.entity;

import com.sparta.omin.app.model.ai.code.RequestType;
import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_ai_log")
public class AiLog extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name="id", updatable = false, nullable = false)
    private UUID id;

    @Column(name="input", updatable = false, nullable = false)
    private String input;

    @Column(name="output", updatable = false, nullable=false)
    private String output;

    @Column(name="request_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Column(name="user_id", nullable = false)
    private UUID userId;

    @Builder
    public AiLog (String input, String output, RequestType requestType, UUID userId) {
        this.input = input;
        this.output = output;
        this.requestType = requestType;
        this.userId = userId;
    }
}
