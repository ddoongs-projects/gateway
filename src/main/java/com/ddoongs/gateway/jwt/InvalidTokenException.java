package com.ddoongs.gateway.jwt;

public class InvalidTokenException extends UnauthorizedException {

  public InvalidTokenException() {
    super(ErrorCode.INVALID_TOKEN);
  }
}
