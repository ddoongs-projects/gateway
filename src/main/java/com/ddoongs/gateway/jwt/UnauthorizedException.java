package com.ddoongs.gateway.jwt;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

  private final ErrorCode code;

  public UnauthorizedException(ErrorCode code) {
    super(code.getDefaultMessage());
    this.code = code;
  }
}
