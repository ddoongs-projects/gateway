package com.ddoongs.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

    Map<String, String> errorResponse = Map.of(
        "code", "UNAUTHORIZED",
        "message", "인증이 필요합니다."
    );

    try {
      String body = objectMapper.writeValueAsString(errorResponse);
      DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
      return response.writeWith(Mono.just(buffer));
    } catch (JsonProcessingException e) {
      return response.setComplete();
    }
  }
}