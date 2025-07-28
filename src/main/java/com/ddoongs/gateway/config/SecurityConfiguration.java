package com.ddoongs.gateway.config;

import com.ddoongs.gateway.filter.AuthenticationFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

  private final AuthenticationFilter authenticationFilter;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
        // CORS 허용
        .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

        .authorizeExchange(exchange -> exchange
            .pathMatchers(
                "/actuator/health",
                "/verifications",
                "/verifications/verify",
                "/members",
                "/members/reset-password",
                "/auth/login",
                "/auth/reissue",
                "/auth/token/exchange",
                "/oauth2/authorization/**",
                "/login/oauth2/code/**"
            )
            .permitAll()
            .anyExchange()
            .authenticated()
        )
        .addFilterAt(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .exceptionHandling(exceptionHandling -> 
            exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint))
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000"));
    config.setAllowedMethods(List.of("*")); // 모든 HTTP 메서드 허용
    config.setAllowedHeaders(List.of("*")); // 모든 헤더 허용
    config.setAllowCredentials(false); // 자격 증명 미허용 (true로 하면 allowedOrigins에 * 사용 불가)

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
