package com.sparta.omin.app.security.jwt;

import com.sparta.omin.app.model.user.dto.response.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secretKey;
	private SecretKey signingKey;

	private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60; // 1시간
	private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 12; // 12시간
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String TOKEN_PREFIX = "Bearer ";

	@PostConstruct
	public void init() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.signingKey = Keys.hmacShaKeyFor(keyBytes);
	}

	public TokenResponse generateToken(UUID userId, String email, String role) {
		return new TokenResponse(
			this.generateAccessToken(userId, email, role),
			this.generateRefreshToken(userId)
		);
	}

	public String generateAccessToken(UUID userId, String email, String role) {
		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim("email", email)
			.claim("roles", role)
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
			.signWith(signingKey)
			.compact();
	}

	public String generateRefreshToken(UUID userId) {
		return Jwts.builder()
			.subject(String.valueOf(userId))
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
			.signWith(signingKey)
			.compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
			return true;
		} catch (SecurityException | MalformedJwtException e) {
			log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
		} catch (ExpiredJwtException e) {
			log.error("Expired JWT token, 만료된 JWT 토큰 입니다.");
		} catch (UnsupportedJwtException e) {
			log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
		} catch (IllegalArgumentException e) {
			log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
		}
		return false;
	}

	public Claims getClaims(String token) {
		return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
	}

	public String getEmail(String token) {
		return getClaims(token).get("email", String.class);
	}
}
