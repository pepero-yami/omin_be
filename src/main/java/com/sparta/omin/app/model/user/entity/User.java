package com.sparta.omin.app.model.user.entity;

import com.sparta.omin.app.model.user.constants.Role;
import com.sparta.omin.common.entity.BaseTimeEntity;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_user")
public class User extends BaseTimeEntity implements UserDetails {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "nickname", nullable = false)
	@Size(min = 2, max = 10)
	private String nickname;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "role")
	@Enumerated(EnumType.STRING)
	private Role role;

	@Column(name = "deleted_by") //FIXME 이거 삭제되는거죠?
	private UUID deletedBy;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return role.getRoles().stream()
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toList());
	}

	@Override
	public String getUsername() {
		return this.email;
	}

	@Builder
	public User(String name, String nickname, String email, String password) {
		this.name = name;
		this.nickname = nickname;
		this.email = email;
		this.password = password;
		this.role = Role.CUSTOMER;
	}

	public void edit(String nickname, String password, PasswordEncoder passwordEncoder) {
		validateCheckEditRequest(nickname, password);
		this.nickname = nickname != null && !nickname.isBlank() ? nickname : this.nickname;
		this.password = password != null && !password.isBlank() ? passwordEncoder.encode(password) : this.password;
	}

	private void validateCheckEditRequest(String nickname, String password) {
		if (nickname != null && !nickname.isBlank()) {
			if (!nickname.matches("^[a-zA-Z0-9가-힣]{2,10}$")) {
				throw new ApiException(ErrorCode.NICKNAME_POLICY_VIOLATION);
			}
		}

		if (password != null && !password.isBlank()) {
			if (!password.matches(
				"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$")) {
				throw new ApiException(ErrorCode.PASSWORD_POLICY_VIOLATION);
			}
		}
	}

	public void softDelete(UUID id) {
		this.isDeleted = true;
		this.deletedBy = id;
	}

	// 회원가입 시점(인증 전)에는 actorId를 알 수 없어서, UUID=0000...로 created_by/updated_by NOT NULL을 채우기 위한 용도
	public void initAuditFieldsForSignUp(UUID actorId) {
		this.createdBy = actorId;
		this.updatedBy = actorId;
		this.isDeleted = false;
	}
}