package com.april.rag.service.Impl;

import com.april.rag.mapper.OrganizationTagMapper;
import com.april.rag.service.OrgTagCacheService;
import com.april.rag.service.TokenCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2025/12/12 16:02
 * Description:
 */

@Service
public class TokenCacheServiceImpl implements TokenCacheService {

    private static final Logger logger = LoggerFactory.getLogger(TokenCacheServiceImpl.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Redis key前缀
    private static final String TOKEN_PREFIX = "jwt:valid:";
    private static final String USER_TOKENS_PREFIX = "jwt:user:";
    private static final String REFRESH_PREFIX = "jwt:refresh:";
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    /**
     * 缓存有效token信息
     *
     * @param tokenId
     * @param userId
     * @param username
     * @param expireTimeMs
     */
    @Override
    public void cacheToken(String tokenId, String userId, String username, long expireTimeMs) {
        try {
            String key = TOKEN_PREFIX + tokenId;
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("userId", userId);
            tokenInfo.put("username", username);
            tokenInfo.put("expireTime", expireTimeMs);

            // 计算Redis过期时间（比JWT过期时间稍长一点）
            long ttlSeconds = (expireTimeMs - System.currentTimeMillis()) / 1000 + 300; // 多5分钟缓冲

            redisTemplate.opsForValue().set(key, tokenInfo, ttlSeconds, TimeUnit.SECONDS);

            // 同时添加到用户token集合中
            addTokenToUser(userId, tokenId, expireTimeMs);

            logger.debug("Token cached: {} for user: {}", tokenId, username);
        } catch (Exception e) {
            logger.error("Failed to cache token: {}", tokenId, e);
        }
    }

    /**
     * 添加token到用户集合
     */
    private void addTokenToUser(String userId, String tokenId, long expireTimeMs) {
        try {
            String key = USER_TOKENS_PREFIX + userId + ":tokens";
            redisTemplate.opsForSet().add(key, tokenId);

            // 设置过期时间
            long ttlSeconds = (expireTimeMs - System.currentTimeMillis()) / 1000 + 300;
            redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            logger.error("Failed to add token to user set: {} - {}", userId, tokenId, e);
        }
    }

    /**
     * 缓存refresh token
     *
     * @param refreshTokenId
     * @param userId
     * @param tokenId
     * @param expireTimeMs
     */
    @Override
    public void cacheRefreshToken(String refreshTokenId, String userId, String tokenId, long expireTimeMs) {
        try {
            String key = REFRESH_PREFIX + refreshTokenId;
            Map<String, Object> refreshInfo = new HashMap<>();
            refreshInfo.put("userId", userId);
            refreshInfo.put("refreshTokenId", refreshTokenId);
            refreshInfo.put("expireTime", expireTimeMs);

            // 设置过期时间
            long ttlSeconds = (expireTimeMs - System.currentTimeMillis()) / 1000 + 300;
            redisTemplate.opsForValue().set(key, refreshInfo, ttlSeconds, TimeUnit.SECONDS);
            logger.debug("Refresh token cached: {} for user: {}", refreshTokenId, userId);
        } catch (Exception e) {
            logger.error("Failed to cache refresh token: {}", refreshTokenId, e);
        }
    }

    /**
     * 验证token是否有效（未被拉黑且存在于缓存中）
     *
     * @param tokenId
     */
    @Override
    public boolean isTokenValid(String tokenId) {
        try {
            // 先检查是否在黑名单中
            if (isTokenBlacklisted(tokenId)) {
                return false;
            }

            // 检查缓存中是否存在
            String key = TOKEN_PREFIX + tokenId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("Failed to check token validity: {}", tokenId, e);
            return false;
        }
    }

    /**
     * 从缓存中获取token信息
     *
     * @param tokenId
     */
    @Override
    public Map<String, Object> getTokenInfo(String tokenId) {
        try {
            String key = TOKEN_PREFIX + tokenId;
            Object tokenInfo = redisTemplate.opsForValue().get(key);
            return tokenInfo != null ? (Map<String, Object>) tokenInfo : null;
        } catch (Exception e) {
            logger.error("Failed to get token info: {}", tokenId, e);
            return null;
        }
    }

    /**
     * 验证refresh token是否有效
     *
     * @param refreshTokenId
     */
    @Override
    public boolean isRefreshTokenValid(String refreshTokenId) {
        try {
            String key = REFRESH_PREFIX + refreshTokenId;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            logger.error("Failed to check refresh token validity: {}", refreshTokenId, e);
            return false;
        }
    }

    /**
     * 获取refresh token信息
     *
     * @param refreshTokenId
     */
    @Override
    public Map<String, Object> getRefreshTokenInfo(String refreshTokenId) {
        try {
            String key = REFRESH_PREFIX + refreshTokenId;
            Object tokenInfo = redisTemplate.opsForValue().get(key);
            return tokenInfo != null ? (Map<String, Object>) tokenInfo : null;
        } catch (Exception e) {
            logger.error("Failed to get refresh token info: {}", refreshTokenId, e);
            return null;
        }
    }

    /**
     * 将token加入黑名单（主动失效）
     *
     * @param tokenId
     * @param expireTimeMs
     */
    @Override
    public void blacklistToken(String tokenId, long expireTimeMs) {
        try {
            String key = BLACKLIST_PREFIX + tokenId;
            long ttlSeconds = Math.max((expireTimeMs - System.currentTimeMillis()) / 1000, 0) ;

            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set(key, System.currentTimeMillis(), ttlSeconds, TimeUnit.SECONDS);
                logger.debug("Token blacklisted: {}", tokenId);
            }
        } catch (Exception e) {
            logger.error("Failed to blacklist token: {}", tokenId, e);
        }
    }

    /**
     * 检查token是否在黑名单中
     *
     * @param tokenId
     */
    @Override
    public boolean isTokenBlacklisted(String tokenId) {
        try {
            String key = BLACKLIST_PREFIX + tokenId;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            logger.error("Failed to check token blacklist: {}", tokenId, e);
            return false;
        }
    }

    /**
     * 移除token缓存
     *
     * @param tokenId
     * @param userId
     */
    @Override
    public void removeToken(String tokenId, String userId) {
        try {
            redisTemplate.delete(TOKEN_PREFIX + tokenId);

            if (userId != null) {
                removeTokenFromUser(userId, tokenId);
            }

        } catch (Exception e) {
            logger.error("Failed to remove token: {}", tokenId, e);
        }
    }

    /**
     * 从用户集合中移除token
     */
    private void removeTokenFromUser(String userId, String tokenId) {
        try {
            String key = USER_TOKENS_PREFIX + userId + ":tokens";
            redisTemplate.opsForSet().remove(key, tokenId);
        } catch (Exception e) {
            logger.error("Failed to remove token from user set: {} - {}", userId, tokenId, e);
        }
    }

    /**
     * 移除用户的所有token（批量登出）
     *
     * @param userId
     */
    @Override
    public void removeAllUserTokens(String userId) {
        try {
            String userTokens =USER_TOKENS_PREFIX + userId + ":tokens";
            Set<Object> tokenSets = redisTemplate.opsForSet().members(userTokens);

            if (tokenSets != null) {
                for (Object tokenSet : tokenSets) {
                    removeToken(tokenSet.toString(), null);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to remove all user tokens: {}", userId, e);
        }
    }

    /**
     * 获取用户的活跃token数量
     *
     * @param userId
     */
    @Override
    public long getUserActiveTokenCount(String userId) {
        try {
            String key = USER_TOKENS_PREFIX + userId + ":tokens";
            Long count = redisTemplate.opsForSet().size(key);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("Failed to get user active token count: {}", userId, e);
            return 0;
        }
    }
}
