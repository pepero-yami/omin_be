package com.sparta.omin.app.model.admin.entity;

import com.sparta.omin.app.model.admin.constants.Department;
import com.sparta.omin.app.model.user.constants.Role;
import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_admin")
public class Admin extends BaseEntity implements UserDetails {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "department", nullable = false)
	@Enumerated(EnumType.STRING)
	private Department department;

	@Column(name = "role")
	@Enumerated(EnumType.STRING)
	private Role role;

	@Builder
	public Admin(String name, String email, String password, Department department) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.department = department;
		this.role = Role.MANAGER;
		this.isDeleted = false;
	}

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
}