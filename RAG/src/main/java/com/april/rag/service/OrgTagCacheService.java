package com.april.rag.service;

import java.util.List;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2025/12/8 15:15
 * Description:组织标签缓存服务
 * 用于缓存用户组织标签信息，提高权限验证效率
 */

public interface OrgTagCacheService {

    /**
     * 缓存用户的组织标签
     *
     * @param username 用户名
     * @param orgTags 组织标签列表
     */
    public void cacheUserOrgTags(String username, List<String> orgTags);

    /**
     * 获取用户的组织标签
     *
     * @param username 用户名
     * @return 组织标签列表
     */
    public List<String> getUserOrgTags(String username);

    /**
     * 缓存用户的主组织标签
     *
     * @param username 用户名
     * @param primaryOrg 主组织标签
     */
    public void cacheUserPrimaryOrg(String username, String primaryOrg);

    /**
     * 获取用户的主组织标签
     *
     * @param username 用户名
     * @return 主组织标签
     */
    public String getUserPrimaryOrg(String username);

    /**
     * 删除用户的组织标签缓存
     *
     * @param username 用户名
     */
    public void deleteUserOrgTagsCache(String username);

    /**
     * 获取用户的有效标签权限集合（包含用户直接拥有的标签及其所有父标签）
     *
     * @param username 用户名
     * @return 用户的有效标签集合
     */
    public List<String> getUserEffectiveOrgTags(String username);

    /**
     * 删除用户有效标签缓存
     */
    public void deleteUserEffectiveTagsCache(String username);

    /**
     * 清除所有用户的有效标签缓存
     * 在组织标签结构变更时调用
     */
    public void invalidateAllEffectiveTagsCache();
}
