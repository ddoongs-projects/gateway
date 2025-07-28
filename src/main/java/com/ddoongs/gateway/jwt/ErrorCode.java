package com.ddoongs.gateway.jwt;

import lombok.Getter;

@Getter
public enum ErrorCode {
  NOT_FOUND("해당 리소스를 찾을 수 없습니다."),
  EXPIRED_TOKEN("토큰이 만료되었습니다."),
  INVALID_TOKEN("토큰이 유효하지 않습니다.");

  private final String defaultMessage;

  ErrorCode(String defaultMessage) {
    this.defaultMessage = defaultMessage;
  }
}
