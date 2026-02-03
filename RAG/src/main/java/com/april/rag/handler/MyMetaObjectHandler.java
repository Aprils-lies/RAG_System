package com.april.rag.handler;

import com.april.rag.entity.User;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2025/12/5 21:20
 * Description:Mybatis-plus自动填充器
 */

@Configuration
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.hasGetter("createTime")) {
            strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        }

        if (metaObject.hasGetter("updateTime")) {
            strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }

        if (metaObject.hasGetter("mergedAt")) {
            strictInsertFill(metaObject, "mergedAt", LocalDateTime.class, LocalDateTime.now());
        }

        // 如果需要设置默认角色
        if (metaObject.hasGetter("role") && metaObject.getValue("role") == null) {
            metaObject.setValue("role", User.Role.USER);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasGetter("updateTime")) {
            strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }

        if (metaObject.hasGetter("mergedAt")) {
            strictUpdateFill(metaObject, "mergedAt", LocalDateTime.class, LocalDateTime.now());
        }
    }
}
