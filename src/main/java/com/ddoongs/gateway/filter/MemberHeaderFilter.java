package com.ddoongs.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MemberHeaderFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
        // 1. SecurityContextHolder에서 컨텍스트를 가져옵니다.
        .filter(sc -> sc.getAuthentication() != null) // 2. 인증(Authentication) 객체가 있는지 확인합니다.
        .flatMap(sc -> {
          Authentication authentication = sc.getAuthentication();

          MemberPrinciple principal = (MemberPrinciple) authentication.getPrincipal();

          Long memberId = principal.memberId();
          String memberEmail = principal.memberEmail();

          // 4. 요청을 수정하여 새로운 헤더들을 추가합니다.
          ServerWebExchange mutatedExchange = exchange.mutate()
              .request(builder -> builder
                  .header("X-Member-Id", String.valueOf(memberId))
                  .header("X-Member-Email", memberEmail))
              .build();

          return chain.filter(mutatedExchange);
        })
        .switchIfEmpty(chain.filter(exchange));
  }

  @Override
  public int getOrder() {
    // GlobalFilter의 실행 순서를 Security 필터 이후로 지정
    return -1; // 높은 우선순위
  }
}
