package com.sparta.omin.app.model.ai.entity;

import com.sparta.omin.app.model.ai.code.RequestType;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiLog extends BaseTimeEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Builder
    public AiLog (String input, String output, RequestType requestType, User user) {
        this.input = input;
        this.output = output;
        this.requestType = requestType;
        this.user = user;
    }
}
