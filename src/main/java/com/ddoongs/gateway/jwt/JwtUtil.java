package com.ddoongs.gateway.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final SecretKey secretKey;

  public JwtUtil(
      @Value("${spring.jwt.secret}") String secret
  ) {
    this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }

  public String getMemberEmail(String token) {
    return (String) getClaims(token).get("email");
  }

  public Long getMemberId(String token) {
    return Long.parseLong(getClaims(token).getSubject());
  }

  private Claims getClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException();
    } catch (JwtException | IllegalArgumentException e) {
      throw new InvalidTokenException();
    }
  }

  public String extractJti(String token) {
    return getClaims(token).getId();
  }
}
