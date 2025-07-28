package com.ddoongs.gateway.error;

import com.ddoongs.gateway.jwt.ErrorCode;
import com.ddoongs.gateway.jwt.UnauthorizedException;
import io.netty.handler.timeout.TimeoutException;
import java.net.ConnectException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-1) // Spring의 기본 ErrorWebExceptionHandler보다 먼저 실행되도록 순서를 높게 설정
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

  public GlobalExceptionHandler(ErrorAttributes errorAttributes,
      ApplicationContext applicationContext,
      ServerCodecConfigurer serverCodecConfigurer) {
    super(errorAttributes, new WebProperties.Resources(), applicationContext);
    super.setMessageWriters(serverCodecConfigurer.getWriters());
    super.setMessageReaders(serverCodecConfigurer.getReaders());
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(
      final ErrorAttributes errorAttributes) {
    return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
  }

  private Mono<ServerResponse> renderErrorResponse(final ServerRequest request) {
    final Map<String, Object> errorPropertiesMap = getErrorAttributes(request,
        ErrorAttributeOptions.defaults());
    final Throwable ex = getError(request);
    // 중요: 전체 스택 트레이스를 로그로 남겨야 디버깅이 용이합니다.
    log.error("Gateway Global Exception Handler", ex);

    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    String errorCode = "INTERNAL_SERVER_ERROR";
    String errorMessage = "서버 내부 오류입니다.";

    // 1. Spring WebFlux 표준 예외 처리
    if (ex instanceof ResponseStatusException resEx) {
      status = (HttpStatus) resEx.getStatusCode();
      // ResponseStatusException은 reason을 메시지로 사용하는 것이 일반적입니다.
      errorMessage = resEx.getReason() != null ? resEx.getReason() : status.getReasonPhrase();
      errorCode = status.name();
    }
    // 2. 잘못된 요청 처리
    else if (ex instanceof ServerWebInputException) {
      status = HttpStatus.BAD_REQUEST;
      errorCode = "BAD_REQUEST";
      errorMessage = "잘못된 요청입니다.";
    }
    // 3. 다운스트림 연결 불가
    else if (ex instanceof ConnectException) {
      status = HttpStatus.SERVICE_UNAVAILABLE;
      errorCode = "SERVICE_UNAVAILABLE";
      errorMessage = "서비스에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.";
    }
    // 4. 다운스트림 응답 시간 초과
    else if (ex instanceof TimeoutException) {
      status = HttpStatus.GATEWAY_TIMEOUT;
      errorCode = "GATEWAY_TIMEOUT";
      errorMessage = "서비스 응답이 지연되고 있습니다.";
    }
    // 5. 리소스를 찾을 수 없음 (기존 로직)
    else if (ex instanceof NoResourceFoundException) {
      status = HttpStatus.NOT_FOUND;
      errorCode = ErrorCode.NOT_FOUND.name();
      errorMessage = ErrorCode.NOT_FOUND.getDefaultMessage();
    }
    // 6. 커스텀 인증 예외 (기존 로직)
    else if (ex instanceof UnauthorizedException unauthorizedException) {
      status = HttpStatus.UNAUTHORIZED;
      errorCode = unauthorizedException.getCode().name();
      errorMessage = unauthorizedException.getCode().getDefaultMessage();
    }

    errorPropertiesMap.clear(); // 기본 속성은 지우고 커스텀 속성만 사용
    errorPropertiesMap.put("code", errorCode);
    errorPropertiesMap.put("message", errorMessage);

    // 5. ServerResponse 객체 생성 후 반환
    return ServerResponse.status(status)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(errorPropertiesMap));
  }
}
