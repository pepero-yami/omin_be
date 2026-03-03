package com.sparta.omin.app.security.jwt;

import com.sparta.omin.app.model.user.constants.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String tokenValue = resolveToken(request);

		if (StringUtils.hasText(tokenValue)) {
			try {
				if (!jwtUtil.validateToken(tokenValue)) {
					log.error("Invalid JWT token: {}", tokenValue);
					filterChain.doFilter(request, response);
					return;
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				return;
			}

			Claims info = jwtUtil.getClaims(tokenValue);

			try {
				setAuthentication(info.getSubject(), info.get("roles", String.class));
			} catch (Exception e) {
				log.error(e.getMessage());
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	public void setAuthentication(String userId, String roleName) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		Authentication authentication = createAuthentication(userId, roleName);
		context.setAuthentication(authentication);

		SecurityContextHolder.setContext(context);
	}

	private Authentication createAuthentication(String userId, String roleName) {
		Role role = Role.valueOf(roleName);

		List<SimpleGrantedAuthority> authorities = role.getRoles().stream()
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toList());

		return new UsernamePasswordAuthenticationToken(userId, null, authorities);
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(JwtUtil.AUTHORIZATION_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtUtil.TOKEN_PREFIX)) {
			return bearerToken.substring(7);
		}
		return null;
	}
}