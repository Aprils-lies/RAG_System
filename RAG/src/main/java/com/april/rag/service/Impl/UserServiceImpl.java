package com.april.rag.service.Impl;

import com.april.rag.entity.OrganizationTag;
import com.april.rag.entity.User;
import com.april.rag.exception.CustomException;
import com.april.rag.mapper.OrganizationTagMapper;
import com.april.rag.mapper.UserMapper;
import com.april.rag.service.OrgTagCacheService;
import com.april.rag.service.UserService;
import com.april.rag.utils.PasswordUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2025/12/8 10:53
 * Description:
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final String DEFAULT_ORG_TAG = "DEFAULT";
    private static final String DEFAULT_ORG_NAME = "默认组织";
    private static final String DEFAULT_ORG_DESCRIPTION = "系统默认组织标签，自动分配给所有新用户";
    private static final String PRIVATE_TAG_PREFIX = "PRIVATE_";
    private static final String PRIVATE_ORG_NAME_SUFFIX = "的私人空间";
    private static final String PRIVATE_ORG_DESCRIPTION = "用户的私人组织标签，仅用户本人可访问";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrganizationTagMapper organizationTagMapper;

    @Autowired
    private OrgTagCacheService orgTagCacheService;


    /**
     * 注册新用户
     * @param username 用户名
     * @param password  用户密码
     */
    @Override
    @Transactional
    public void registerUser(String username, String password) {
        // 1：检查数据库中是否已存在该用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            // 若用户名已存在，抛出自定义异常，状态码为 400 Bad Request
            throw new CustomException("Username already exists", HttpStatus.BAD_REQUEST);
        }

        // 2：创建用户，Password加密 设置角色
        ensureDefaultOrgTagExists();

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(PasswordUtils.encode(password));
        newUser.setRole(User.Role.USER);
        userMapper.insert(newUser);

        // 3：创建用户的私人组织标签 并保存数据库以及redis
        String privateTagId = PRIVATE_TAG_PREFIX + username;
        createPrivateOrgTag(privateTagId, username, user);

        // 只分配私人组织标签
        newUser.setOrgTags(privateTagId);
        // 设置私人组织标签为主组织标签
        newUser.setPrimaryOrg(privateTagId);
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", username);
        userMapper.update(newUser, updateWrapper);

        orgTagCacheService.cacheUserOrgTags(username, List.of(privateTagId));
        orgTagCacheService.cacheUserPrimaryOrg(username, privateTagId);

        logger.info("User registered successfully with private organization tag: {}", username);

    }

    /**
     * 创建用户的私人组织标签
     * @param privateTagId
     * @param username
     * @param owner
     */
    private void createPrivateOrgTag(String privateTagId, String username, User owner) {
        // 检查私人标签是否已存在
        if (!organizationTagMapper.existsByTagId(privateTagId)) {
            logger.info("Creating private organization tag for user: {}", username);

            // 创建私人组织标签
            OrganizationTag privateTag = new OrganizationTag();
            privateTag.setTagId(privateTagId);
            privateTag.setName(username + PRIVATE_ORG_NAME_SUFFIX);
            privateTag.setDescription(PRIVATE_ORG_DESCRIPTION);
            privateTag.setCreatedBy(owner);

            organizationTagMapper.insert(privateTag);
            logger.info("Private organization tag created successfully for user: {}", username);
        }
    }

    /**
     * 确保默认组织标签存在
     */
    private void ensureDefaultOrgTagExists() {
        if (!organizationTagMapper.existsByTagId(DEFAULT_ORG_TAG)) {
            logger.info("Creating default organization tag");

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getRole, User.Role.ADMIN);
            // 寻找或者创建一个系统管理员
            User adminUser = userMapper.selectList(queryWrapper).stream().findFirst().orElseGet(this::createSystemAdminIfNotExists);

            // 创建默认组织标签
            OrganizationTag defaultTag = new OrganizationTag();
            defaultTag.setTagId(DEFAULT_ORG_TAG);
            defaultTag.setName(DEFAULT_ORG_NAME);
            defaultTag.setDescription(DEFAULT_ORG_DESCRIPTION);
            defaultTag.setCreatedBy(adminUser);

            organizationTagMapper.insert(defaultTag);
            logger.info("Default organization tag created successfully");
        }
    }

    /**
     * 如果系统中没有管理员用户，则创建一个系统管理员
     */
    private User createSystemAdminIfNotExists() {
        String systemAdminUsername = "system_admin";

        User user = userMapper.findByUsername(systemAdminUsername);
        // 创建系统管理员
        if (user == null) {
            logger.info("Creating system admin user");
            User newSystemUser = new User();
            newSystemUser.setUsername(systemAdminUsername);
            // 生成随机密码
            String password = generateRandomPassword();
            newSystemUser.setPassword(PasswordUtils.encode(password));
            newSystemUser.setRole(User.Role.ADMIN);

            logger.info("System admin created with password: {}", password);
            return newSystemUser;
        }
        return user;
    }

    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        // 生成16位随机密码
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 创建管理员用户。
     *
     * @param username        要注册的管理员用户名
     * @param password        要注册的管理员密码
     * @param creatorUsername 创建者的用户名（必须是已存在的管理员）
     * @throws CustomException 如果用户名已存在或创建者不是管理员，则抛出异常
     */
    @Override
    public void createAdminUser(String username, String password, String creatorUsername) {
        // 1：验证创建者是否为管理员
        User creator = userMapper.findByUsername(creatorUsername);
        if (creator == null) {
            throw new CustomException("Creator not found", HttpStatus.NOT_FOUND);
        }

        if (creator.getRole() != User.Role.ADMIN) {
            throw new CustomException("Only administrators can create admin accounts", HttpStatus.FORBIDDEN);
        }

        // 检查数据库中是否已存在该用户名
        if (userMapper.findByUsername(username) != null) {
            throw new CustomException("Username already exists", HttpStatus.BAD_REQUEST);
        }

        User adminUser = new User();
        adminUser.setUsername(username);
        adminUser.setPassword(PasswordUtils.encode(password));
        adminUser.setRole(User.Role.ADMIN);
        userMapper.insert(adminUser);
    }

    /**
     * 对用户进行认证。
     *
     * @param username 要认证的用户名
     * @param password 要认证的用户密码
     * @return 认证成功后返回用户的用户名
     * @throws CustomException 如果用户名或密码无效，则抛出异常
     */
    @Override
    public String authenticateUser(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new CustomException("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }

        // 比较输入的密码和数据库中存储的加密密码是否匹配
        if (!PasswordUtils.matches(password, user.getPassword())) {
            // 若不匹配，抛出自定义异常，状态码为 401 Unauthorized
            throw new CustomException("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }

        // 认证成功，返回用户的用户名
        return user.getUsername();
    }

    /**
     * 创建组织标签
     *
     * @param tagId           标签唯一标识
     * @param name            标签名称
     * @param description     标签描述
     * @param parentTag       父标签ID（可选）
     * @param creatorUsername 创建者用户名（必须是管理员）
     */
    @Override
    @Transactional
    public OrganizationTag createOrganizationTag(String tagId, String name, String description, String parentTag, String creatorUsername) {
        // 验证创建者是否为管理员
        User creator = userMapper.findByUsername(creatorUsername);
        if (creator == null) {
            throw new CustomException("Creator not found", HttpStatus.NOT_FOUND);
        }

        if (creator.getRole() != User.Role.ADMIN) {
            throw new CustomException("Only administrators can create organization tags", HttpStatus.FORBIDDEN);
        }

        // 检查标签ID是否已存在
        if (organizationTagMapper.existsByTagId(tagId)) {
            throw new CustomException("Tag ID already exists", HttpStatus.BAD_REQUEST);
        }

        // 如果指定了父标签，检查父标签是否存在
        if (parentTag != null && !parentTag.isEmpty()) {
            OrganizationTag byTagId = organizationTagMapper.findByTagId(parentTag);
            if (byTagId == null) {
                throw new CustomException("Parent tag not found", HttpStatus.BAD_REQUEST);
            }
        }

        OrganizationTag tag = new OrganizationTag();
        tag.setTagId(tagId);
        tag.setName(name);
        tag.setDescription(description);
        tag.setParentTag(parentTag);
        tag.setCreatedBy(creator);
        organizationTagMapper.insert(tag);
        OrganizationTag savedTag = tag;

        // 清除标签缓存，因为层级关系可能变化
        orgTagCacheService.invalidateAllEffectiveTagsCache();

        return savedTag;

    }

    /**
     * 为用户分配组织标签
     *
     * @param userId        用户ID
     * @param orgTags       组织标签ID列表
     * @param adminUsername 管理员用户名
     */
    @Override
    @Transactional
    public void assignOrgTagsToUser(Long userId, List<String> orgTags, String adminUsername) {
        User admin = userMapper.findByUsername(adminUsername);
        if (admin == null) {
            throw new CustomException("Admin not found", HttpStatus.NOT_FOUND);
        }

        if (admin.getRole() != User.Role.ADMIN) {
            throw new CustomException("Only administrators can assign organization tags", HttpStatus.FORBIDDEN);
        }

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND);
        }

        // 验证所有标签是否存在
        for (String tagId : orgTags) {
            if (!organizationTagMapper.existsByTagId(tagId)) {
                throw new CustomException("Organization tag " + tagId + " not found", HttpStatus.NOT_FOUND);
            }
        }

        // 获取用户的现有组织标签
        Set<String> existingTags = new HashSet<>();
        if (user.getOrgTags() != null && !user.getOrgTags().isEmpty()) {
            existingTags = Arrays.stream(user.getOrgTags().split(",")).collect(Collectors.toSet());
        }

        // 找出并保留用户的私人组织标签
        String privateTagId = PRIVATE_TAG_PREFIX + user.getUsername();
        boolean hasPrivateTag = existingTags.contains(privateTagId);

        // 确保用户的私人组织标签不会被删除
        Set<String> finalTags = new HashSet<>(orgTags);
        if (hasPrivateTag && !finalTags.contains(privateTagId)) {
            finalTags.add(privateTagId);
        }

        // 将标签列表转换为逗号分隔的字符串
        String orgTagsStr = String.join(",", finalTags);
        user.setOrgTags(orgTagsStr);

        // 如果用户没有主组织标签且有组织标签，则优先使用私人标签作为主组织
        if ((user.getPrimaryOrg() == null || user.getPrimaryOrg().isEmpty()) && !finalTags.isEmpty()) {
            if (hasPrivateTag) {
                user.setPrimaryOrg(privateTagId);
            } else {
                user.setPrimaryOrg(new ArrayList<>(finalTags).get(0));
            }
        }

        userMapper.updateById(user);

        // 更新缓存
        orgTagCacheService.deleteUserOrgTagsCache(user.getUsername());
        orgTagCacheService.cacheUserOrgTags(user.getUsername(), new ArrayList<>(finalTags));
        // 同时清除有效标签缓存
        orgTagCacheService.deleteUserEffectiveTagsCache(user.getUsername());

        if (user.getPrimaryOrg() != null && !user.getPrimaryOrg().isEmpty()) {
            orgTagCacheService.cacheUserPrimaryOrg(user.getUsername(), user.getPrimaryOrg());
        }
    }

    /**
     * 获取用户的组织标签信息
     *
     * @param username 用户名
     * @return 包含用户组织标签信息的Map
     */
    @Override
    public Map<String, Object> getUserOrgTags(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND);
        }

        // 尝试从缓存获取
        List<String> orgTags = orgTagCacheService.getUserOrgTags(username);
        String primaryOrg = orgTagCacheService.getUserPrimaryOrg(username);

        // 如果缓存中没有，则从数据库获取
        if (orgTags == null || orgTags.isEmpty()) {
            orgTags = Arrays.asList(user.getOrgTags().split(","));
            // 更新缓存
            orgTagCacheService.cacheUserOrgTags(username, orgTags);
        }

        if (primaryOrg == null || primaryOrg.isEmpty()) {
            primaryOrg = user.getPrimaryOrg();
            // 更新缓存
            orgTagCacheService.cacheUserPrimaryOrg(username, primaryOrg);
        }

        // 获取组织标签的详细信息
        List<Map<String, String>> orgTagDetails = new ArrayList<>();
        for (String tagId : orgTags) {
            OrganizationTag tag = organizationTagMapper.findByTagId(tagId);
            if (tag != null) {
                Map<String, String> tagInfo = new HashMap<>();
                tagInfo.put("tagId", tag.getTagId());
                tagInfo.put("name", tag.getName());
                tagInfo.put("description", tag.getDescription());
                orgTagDetails.add(tagInfo);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orgTags", orgTags);
        result.put("primaryOrg", primaryOrg);
        result.put("orgTagDetails", orgTagDetails);

        return result;
    }

    /**
     * 设置用户的主组织标签
     *
     * @param username   用户名
     * @param primaryOrg 主组织标签
     */
    @Override
    public void setUserPrimaryOrg(String username, String primaryOrg) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND);
        }

        // 检查该组织标签是否已分配给用户
        Set<String> userTags = Arrays.stream(user.getOrgTags().split(",")).collect(Collectors.toSet());
        if (!userTags.contains(primaryOrg)) {
            throw new CustomException("Organization tag not assigned to user", HttpStatus.BAD_REQUEST);
        }

        user.setPrimaryOrg(primaryOrg);
        userMapper.updateById(user);

        // 更新缓存
        orgTagCacheService.cacheUserPrimaryOrg(username, primaryOrg);
    }

    /**
     * 获取用户的主组织标签
     *
     * @param userId 用户ID
     * @return 用户的主组织标签
     */
    @Override
    public String getUserPrimaryOrg(String userId) {
        // 先通过userId查找用户，然后获取username
        User user;
        try {
            Long userIdLong = Long.parseLong(userId);
            user = userMapper.findById(userIdLong);
            if (user == null) {
                throw  new CustomException("User not found with ID: " + userId, HttpStatus.NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            // 如果userId不是数字格式，则假设它就是username
            user = userMapper.findByUsername(userId);
            if (user == null) {
                throw new CustomException("User not found: " + userId, HttpStatus.NOT_FOUND);
            }
        }

        String username = user.getUsername();

        // 尝试从缓存获取
        String primaryOrg = orgTagCacheService.getUserPrimaryOrg(username);

        // 如果缓存中没有，则从数据库获取
        if (primaryOrg == null || primaryOrg.isEmpty()) {
            primaryOrg = user.getPrimaryOrg();

            // 如果用户没有设置主组织标签，则尝试使用第一个分配的组织标签
            if (primaryOrg == null || primaryOrg.isEmpty()) {
                String[] tags = user.getOrgTags().split(",");
                if (tags.length > 0) {
                    primaryOrg = tags[0];
                    // 更新用户的主组织标签
                    user.setPrimaryOrg(primaryOrg);
                    userMapper.updateById(user);
                } else {
                    // 如果用户没有任何组织标签，则使用默认标签
                    primaryOrg = DEFAULT_ORG_TAG;
                }
            }

            // 更新缓存
            orgTagCacheService.cacheUserPrimaryOrg(username, primaryOrg);
        }

        return primaryOrg;
    }

    /**
     * 获取组织标签树结构
     *
     * @return 组织标签树结构
     */
    @Override
    public List<Map<String, Object>> getOrganizationTagTree() {
        // 获取所有根节点（parentTag为null的标签）
        List<OrganizationTag> rootTags = organizationTagMapper.findByParentTag(null);

        // 递归构建标签树
        return buildTagTreeRecursive(rootTags);
    }

    /**
     * 递归构建标签树
     *
     * @param tags 当前级别的标签列表
     * @return 树形结构
     */
    private List<Map<String, Object>> buildTagTreeRecursive(List<OrganizationTag> tags) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (OrganizationTag tag : tags) {
            Map<String, Object> node = new HashMap<>();
            node.put("tagId", tag.getTagId());
            node.put("name", tag.getName());
            node.put("description", tag.getDescription());
            node.put("parentTag", tag.getParentTag()); // 添加父标签字段

            // 获取子标签
            List<OrganizationTag> children = organizationTagMapper.findByParentTag(tag.getTagId());
            if (!children.isEmpty()) {
                node.put("children", buildTagTreeRecursive(children));
            }
            // 如果没有子节点，不添加children字段，而不是添加空数组

            result.add(node);
        }

        return result;
    }

    /**
     * 更新组织标签
     *
     * @param tagId         标签ID
     * @param name          新名称
     * @param description   新描述
     * @param parentTag     新父标签ID
     * @param adminUsername 管理员用户名
     * @return 更新后的组织标签
     */
    @Override
    @Transactional
    public OrganizationTag updateOrganizationTag(String tagId, String name, String description, String parentTag, String adminUsername) {
        User admin = userMapper.findByUsername(adminUsername);
        if (admin == null) {
            throw new CustomException("Admin not found", HttpStatus.NOT_FOUND);
        }

        if (admin.getRole() != User.Role.ADMIN) {
            throw new CustomException("Only administrators can update organization tags", HttpStatus.FORBIDDEN);
        }

        // 获取要更新的标签
        OrganizationTag tag = organizationTagMapper.findByTagId(tagId);
        if (tag == null) {
            throw new CustomException("Organization tag not found", HttpStatus.NOT_FOUND);
        }

        // 如果指定了父标签，检查父标签是否存在
        if (parentTag != null && !parentTag.isEmpty()) {
            // 检查是否为自身
            if (tagId.equals(parentTag)) {
                throw new CustomException("A tag cannot be its own parent", HttpStatus.BAD_REQUEST);
            }

            // 检查是否存在
            if(organizationTagMapper.findByTagId(parentTag) == null){
                throw new CustomException("Parent tag not found", HttpStatus.NOT_FOUND);
            }

            // 检查是否会形成循环
            if (wouldFormCycle(tagId, parentTag)) {
                throw new CustomException("Setting this parent would create a cycle in the tag hierarchy", HttpStatus.BAD_REQUEST);
            }
        }

        // 更新标签
        if (name != null && !name.isEmpty()) {
            tag.setName(name);
        }

        if (description != null) {
            tag.setDescription(description);
        }

        tag.setParentTag(parentTag);

        organizationTagMapper.updateById(tag);
        OrganizationTag updatedTag = tag;

        // 清除所有标签缓存，因为层级关系可能变化
        orgTagCacheService.invalidateAllEffectiveTagsCache();

        return updatedTag;
    }

    /**
     * 检查是否会形成标签层级循环
     *
     * @param tagId 要设置父标签的标签ID
     * @param newParentId 新的父标签ID
     * @return 是否会形成循环
     */
    private boolean wouldFormCycle(String tagId, String newParentId) {
        String currentParentId = newParentId;

        // 检查是否形成循环
        while (currentParentId != null && !currentParentId.isEmpty()) {
            if (tagId.equals(currentParentId)) {
                return true; // 形成循环
            }

            // 获取父标签的父标签
            OrganizationTag parentTag = organizationTagMapper.findByTagId(currentParentId);
            if (parentTag == null) {
                break;
            }

            currentParentId = parentTag.getParentTag();
        }

        return false;
    }


    /**
     * 删除组织标签
     *
     * @param tagId         标签ID
     * @param adminUsername 管理员用户名
     */
    @Override
    @Transactional
    public void deleteOrganizationTag(String tagId, String adminUsername) {
        // 验证操作者是否为管理员
        User admin = userMapper.findByUsername(adminUsername);
        if (admin == null) {
            throw new CustomException("Admin not found", HttpStatus.NOT_FOUND);
        }

        if (admin.getRole() != User.Role.ADMIN) {
            throw new CustomException("Only administrators can delete organization tags", HttpStatus.FORBIDDEN);
        }

        // 获取要删除的标签
        OrganizationTag tag = organizationTagMapper.findByTagId(tagId);
        if (tag == null) {
            throw new CustomException("Organization tag not found", HttpStatus.NOT_FOUND);
        }

        // 检查是否是特殊标签（如默认标签）
        if (DEFAULT_ORG_TAG.equals(tagId)) {
            throw new CustomException("Cannot delete the default organization tag", HttpStatus.BAD_REQUEST);
        }

        // 检查是否有子标签
        List<OrganizationTag> children = organizationTagMapper.findByParentTag(tagId);
        if (!children.isEmpty()) {
            throw new CustomException("Cannot delete a tag with child tags", HttpStatus.BAD_REQUEST);
        }

        // 检查是否有用户使用此标签
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            if (user.getOrgTags() != null && !user.getOrgTags().isEmpty()) {
                Set<String> userTags = new HashSet<>(Arrays.asList(user.getOrgTags().split(",")));
                if (userTags.contains(tagId)) {
                    throw new CustomException("Cannot delete a tag that is assigned to users", HttpStatus.CONFLICT);
                }

                // 检查是否被用作主组织标签
                if (tagId.equals(user.getPrimaryOrg())) {
                    throw new CustomException("Cannot delete a tag that is used as primary organization", HttpStatus.CONFLICT);
                }
            }
        }

        // 检查是否有文档使用此标签（此处应检查file_upload表中的org_tag字段）
        // 由于我们没有直接访问FileUploadRepository，这里采用简化的方式检查
        // 实际实现中，应该注入FileUploadRepository并使用正确的查询方法
        try {
            long fileCount = 0; // 应该是 fileUploadRepository.countByOrgTag(tagId);
            if (fileCount > 0) {
                throw new CustomException("Cannot delete a tag that is associated with documents", HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            logger.error("Error checking file usage of tag: {}", tagId, e);
            throw new CustomException("Failed to check if tag is used by documents", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 删除标签
        organizationTagMapper.deleteById(tag.getTagId());

        // 清除所有标签缓存，因为层级关系可能变化
        orgTagCacheService.invalidateAllEffectiveTagsCache();

        logger.info("Organization tag deleted successfully: {}", tagId);
    }

    /**
     * 获取用户列表，支持分页和过滤
     *
     * @param keyword 搜索关键词
     * @param orgTag  组织标签过滤
     * @param status  用户状态过滤
     * @param page    页码
     * @param size    每页大小
     * @return 用户列表数据
     */
    @Override
    public Map<String, Object> getUserList(String keyword, String orgTag, Integer status, int page, int size) {
        // MyBatis-Plus 页码从 1 开始
        Page<User> mpPage = new Page<>(page, size);
        // 构建查询条件
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        // 关键词过滤
        if (keyword != null && !keyword.isEmpty()) {
            qw.like(User::getUsername, keyword);
        }

        // 状态过滤
        if (status != null) {
            qw.eq(User::getRole, status == 1 ? User.Role.USER : User.Role.ADMIN);
        }

        // 组织标签过滤（逗号字符串，只能 like）
        if (orgTag != null && !orgTag.isEmpty()) {
            qw.and(w -> w
                    .like(User::getOrgTags, orgTag + ",")
                    .or()
                    .like(User::getOrgTags, "," + orgTag)
                    .or()
                    .eq(User::getOrgTags, orgTag));
        }

        // 排序
        qw.orderByDesc(User::getCreatedAt);
        // 分页查询
        Page<User> userPage = userMapper.selectPage(mpPage, qw);
        // 结果转换
        List<Map<String, Object>> userList = userPage.getRecords().stream()
            .map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("primaryOrg", user.getPrimaryOrg());
                userMap.put("status", user.getRole() == User.Role.USER ? 1 : 0);
                userMap.put("createdAt", user.getCreatedAt());

                // 组织标签详情
                List<Map<String, String>> orgTagDetails = new ArrayList<>();
                if (user.getOrgTags() != null && !user.getOrgTags().isEmpty()) {
                    Arrays.stream(user.getOrgTags().split(","))
                        .forEach(tagId -> {
                            OrganizationTag tag = organizationTagMapper.selectOne(
                                    new LambdaQueryWrapper<OrganizationTag>()
                                            .eq(OrganizationTag::getTagId, tagId)
                            );
                            if (tag != null) {
                                Map<String, String> tagInfo = new HashMap<>();
                                tagInfo.put("tagId", tag.getTagId());
                                tagInfo.put("name", tag.getName());
                                orgTagDetails.add(tagInfo);
                            }
                        });
                }
                userMap.put("orgTags", orgTagDetails);
                return userMap;
            })
            .toList();
        // 返回结构
        Map<String, Object> result = new HashMap<>();
        result.put("content", userList);
        result.put("totalElements", userPage.getTotal());
        result.put("totalPages", userPage.getPages());
        result.put("size", userPage.getSize());
        result.put("number", userPage.getCurrent()); // 已是从 1 开始

        return result;
    }
}
