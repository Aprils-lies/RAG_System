package com.april.rag.service;

import com.april.rag.entity.OrganizationTag;
import com.april.rag.exception.CustomException;

import java.util.List;
import java.util.Map;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2025/12/8 10:52
 * Description:
 */

public interface UserService {

    /**
     * 注册新用户
     * @param username 用户名
     * @param password  用户密码
     */
    void registerUser(String username, String password);

    /**
     * 创建管理员用户。
     *
     * @param username 要注册的管理员用户名
     * @param password 要注册的管理员密码
     * @param creatorUsername 创建者的用户名（必须是已存在的管理员）
     * @throws CustomException 如果用户名已存在或创建者不是管理员，则抛出异常
     */
    void createAdminUser(String username, String password, String creatorUsername);

    /**
     * 对用户进行认证。
     *
     * @param username 要认证的用户名
     * @param password 要认证的用户密码
     * @return 认证成功后返回用户的用户名
     * @throws CustomException 如果用户名或密码无效，则抛出异常
     */
    String authenticateUser(String username, String password);

    /**
     * 创建组织标签
     *
     * @param tagId 标签唯一标识
     * @param name 标签名称
     * @param description 标签描述
     * @param parentTag 父标签ID（可选）
     * @param creatorUsername 创建者用户名（必须是管理员）
     */
    OrganizationTag createOrganizationTag(String tagId, String name, String description,
                                                 String parentTag, String creatorUsername);

    /**
     * 为用户分配组织标签
     *
     * @param userId 用户ID
     * @param orgTags 组织标签ID列表
     * @param adminUsername 管理员用户名
     */
    void assignOrgTagsToUser(Long userId, List<String> orgTags, String adminUsername);

    /**
     * 获取用户的组织标签信息
     *
     * @param username 用户名
     * @return 包含用户组织标签信息的Map
     */
    Map<String, Object> getUserOrgTags(String username);

    /**
     * 设置用户的主组织标签
     *
     * @param username 用户名
     * @param primaryOrg 主组织标签
     */
    void setUserPrimaryOrg(String username, String primaryOrg);

    /**
     * 获取用户的主组织标签
     *
     * @param userId 用户ID
     * @return 用户的主组织标签
     */
    String getUserPrimaryOrg(String userId);

    /**
     * 获取组织标签树结构
     *
     * @return 组织标签树结构
     */
    List<Map<String, Object>> getOrganizationTagTree();

    /**
     * 更新组织标签
     *
     * @param tagId 标签ID
     * @param name 新名称
     * @param description 新描述
     * @param parentTag 新父标签ID
     * @param adminUsername 管理员用户名
     * @return 更新后的组织标签
     */
    OrganizationTag updateOrganizationTag(String tagId, String name, String description,
                                                 String parentTag, String adminUsername);

    /**
     * 删除组织标签
     *
     * @param tagId 标签ID
     * @param adminUsername 管理员用户名
     */
    void deleteOrganizationTag(String tagId, String adminUsername);

    /**
     * 获取用户列表，支持分页和过滤
     *
     * @param keyword 搜索关键词
     * @param orgTag 组织标签过滤
     * @param status 用户状态过滤
     * @param page 页码
     * @param size 每页大小
     * @return 用户列表数据
     */
    public Map<String, Object> getUserList(String keyword, String orgTag, Integer status, int page, int size);
}
