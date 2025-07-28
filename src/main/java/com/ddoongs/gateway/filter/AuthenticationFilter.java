package com.ddoongs.gateway.filter;

import com.ddoongs.gateway.jwt.InvalidTokenException;
import com.ddoongs.gateway.jwt.JwtUtil;
import com.ddoongs.gateway.redis.BlacklistTokenRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements WebFilter {

  private final JwtUtil jwtUtil;
  private final BlacklistTokenRepository blacklistTokenRepository;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String token = extractToken(exchange);

    if (token == null) {
      return chain.filter(exchange);
    }

    if (blacklistTokenRepository.exists(jwtUtil.extractJti(token))) {
      throw new InvalidTokenException();
    }

    Long memberId = jwtUtil.getMemberId(token);
    String memberEmail = jwtUtil.getMemberEmail(token);

    MemberPrinciple memberPrinciple = new MemberPrinciple(memberId, memberEmail);

    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        memberPrinciple,
        null,
        List.of()
    );

    return chain.filter(exchange)
        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
  }

  private String extractToken(ServerWebExchange exchange) {
    String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
