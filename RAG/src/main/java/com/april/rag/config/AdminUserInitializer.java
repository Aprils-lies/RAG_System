package com.april.rag.config;

import com.april.rag.entity.User;
import com.april.rag.mapper.UserMapper;
import com.april.rag.utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/4 19:54
 * Description:管理员账号初始化器,在应用启动时自动创建管理员账号（如果不存在）
 */

@Component
@Order(1) // 设置优先级，确保在其他初始化器之前运行
public class AdminUserInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

    @Autowired
    private UserMapper userMapper;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @Value("${admin.primary-org:default}")
    private String adminPrimaryOrg;

    @Value("${admin.org-tags:default,admin}")
    private String adminOrgTags;

    @Override
    public void run(String... args) throws Exception {
        logger.info("检查管理员账号是否存在: {}", adminUsername);
        User user = userMapper.findByUsername(adminUsername);
        if (user != null) {
            logger.info("管理员账号 '{}' 已存在，跳过创建步骤", adminUsername);
            return;
        }

        try {
            logger.info("开始创建管理员账号: {}", adminUsername);
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(PasswordUtils.encode(adminPassword));
            adminUser.setRole(User.Role.ADMIN);
            adminUser.setPrimaryOrg(adminPrimaryOrg);
            adminUser.setOrgTags(adminOrgTags);

            userMapper.insert(adminUser);
            logger.info("管理员账号 '{}' 创建成功", adminUsername);
        } catch (Exception e) {
            logger.error("创建管理员账号失败: {}", e.getMessage(), e);
            throw new RuntimeException("无法创建管理员账号", e);
        }

    }
}
