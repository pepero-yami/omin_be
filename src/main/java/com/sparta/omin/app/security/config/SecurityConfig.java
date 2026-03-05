package com.sparta.omin.app.security.config;

import com.sparta.omin.app.security.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtFilter jwtFilter;

    /**
     * TODO(auth):
     * 1) 유저/인증 작업 완료 후, permitAll() 제거
     * 2) MANAGER/CUSTOMER Role 기반 인가 정책 추가
     *    예)
     *      - /region/**  -> hasRole("MANAGER")
     *      - /regions    -> hasRole("MANAGER")
     *      - /address/** -> hasRole("CUSTOMER")
     * 3) JWT/세션 등 인증 방식 확정 후, csrf/cors/stateless 정책 정리
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 지금은 개발 편의를 위해 전부 오픈
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                        .permitAll() // 정적 리소스 허용
                        .requestMatchers("/api/v1/users/auth").permitAll()
                        .requestMatchers("/api/v1/users").hasRole("CUSTOMER")
                        .requestMatchers("/api/v1/cart/**").hasRole("CUSTOMER") // 카트 관련 권한 수정

						// Region 조회: CUSTOMER도 가능
						.requestMatchers(HttpMethod.GET, "/api/v1/regions/**").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/v1/regions").hasRole("CUSTOMER")

						// Region 생성/수정/삭제: MASTER만 가능
						.requestMatchers(HttpMethod.POST, "/api/v1/regions").hasRole("MASTER")
						.requestMatchers(HttpMethod.PUT, "/api/v1/regions/**").hasRole("MASTER")
						.requestMatchers(HttpMethod.DELETE, "/api/v1/regions/**").hasRole("MASTER")

						// Region seed 실행: MASTER만 가능
						.requestMatchers(HttpMethod.POST, "/api/v1/region-seeds").hasRole("MASTER")

						//store
						.requestMatchers("/api/v1/stores/*/admin").hasRole("MANAGER")
						.requestMatchers("/api/v1/stores/*/owner").hasRole("OWNER")
						.requestMatchers(HttpMethod.GET,"/api/v1/stores/*").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.DELETE,"/api/v1/stores/*").hasRole("OWNER")
						.requestMatchers(HttpMethod.PUT,"/api/v1/stores/*").hasRole("OWNER")
						.requestMatchers("/api/v1/stores").hasRole("CUSTOMER")

						.anyRequest().permitAll()
                )

                // 개발 단계: Postman 호출 편의상 csrf 끔
                // TODO(auth): 인증 방식 확정 후(세션 기반이면 켜는 것을 고려), 정책 재검토 필요
                .csrf(csrf -> csrf.disable())

                // 기본 로그인 폼/Basic 인증도 불필요하므로 비활성화(완전 오픈 모드)
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.disable());
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}