package com.ddoongs.gateway.jwt;


public class TokenExpiredException extends UnauthorizedException {

  public TokenExpiredException() {
    super(ErrorCode.EXPIRED_TOKEN);
  }
}
