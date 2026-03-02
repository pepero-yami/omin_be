package com.sparta.omin.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
                        .anyRequest().permitAll()
                )

                // 개발 단계: Postman 호출 편의상 csrf 끔
                // TODO(auth): 인증 방식 확정 후(세션 기반이면 켜는 것을 고려), 정책 재검토 필요
                .csrf(csrf -> csrf.disable())

                // 기본 로그인 폼/Basic 인증도 불필요하므로 비활성화(완전 오픈 모드)
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.disable());

        return http.build();
    }
}