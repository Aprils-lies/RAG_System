package com.april.rag.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2025/12/5 20:55
 * Description:
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("users")
@Accessors(chain = true) // 支持链式调用
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @TableField(value = "username")
    private String username;

    @NotBlank(message = "密码不能为空")
    @TableField(value = "password")
    private String password;

    @NotNull(message = "角色不能为空")
    @TableField(value = "role")
    private Role role;

    @TableField(value = "org_tags")
    private String orgTags;

    @TableField(value = "primary_org")
    private String primaryOrg; // 用户主组织标签

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;


    public enum Role {
        USER,
        ADMIN
    }
}
