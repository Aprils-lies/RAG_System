package com.april.rag.service;

import java.util.Map;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2025/12/8 17:27
 * Description:
 */

public interface TokenCacheService {

    /**
     * 缓存有效token信息
     */
    void cacheToken(String tokenId, String userId, String username, long expireTimeMs);

    /**
     * 缓存refresh token
     */
    void cacheRefreshToken(String refreshTokenId, String userId, String tokenId, long expireTimeMs);

    /**
     * 验证token是否有效（未被拉黑且存在于缓存中）
     */
    boolean isTokenValid(String tokenId);

    /**
     * 从缓存中获取token信息
     */
    Map<String, Object> getTokenInfo(String tokenId);

    /**
     * 验证refresh token是否有效
     */
    boolean isRefreshTokenValid(String refreshTokenId);


    /**
     * 获取refresh token信息
     */
    Map<String, Object> getRefreshTokenInfo(String refreshTokenId);

    /**
     * 将token加入黑名单（主动失效）
     */
    void blacklistToken(String tokenId, long expireTimeMs);

    /**
     * 检查token是否在黑名单中
     */
    boolean isTokenBlacklisted(String tokenId);

    /**
     * 移除token缓存
     */
    void removeToken(String tokenId, String userId);


    /**
     * 移除用户的所有token（批量登出）
     */
    void removeAllUserTokens(String userId);

    /**
     * 获取用户的活跃token数量
     */
    long getUserActiveTokenCount(String userId);

}
