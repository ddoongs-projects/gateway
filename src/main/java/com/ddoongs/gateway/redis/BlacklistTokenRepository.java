package com.ddoongs.gateway.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class BlacklistTokenRepository {

  private static final String BLACKLIST_TOKEN_KEY_PREFIX = "blacklistToken:";

  private final StringRedisTemplate stringRedisTemplate;

  public boolean exists(String jti) {
    String key = BLACKLIST_TOKEN_KEY_PREFIX + jti;
    return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
  }

}
