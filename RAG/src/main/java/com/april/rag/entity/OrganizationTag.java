package com.april.rag.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2025/12/5 20:55
 * Description:
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("organization_tags")
public class OrganizationTag {

    /**
     * 标签唯一标识（手工指定 ID，不自增）
     */
    @TableId(value = "tag_id", type = IdType.INPUT)
    private String tagId;

    /**
     * 标签名称
     */
    @TableField("name")
    @NotNull
    private String name;

    /**
     * 描述（TEXT）
     */
    @TableField("description")
    private String description;

    /**
     * 父标签 ID
     */
    @TableField("parent_tag")
    @Size(max = 255)
    private String parentTag;

    /**
     * 创建者用户 ID
     * （由原来的 @ManyToOne User createdBy 改为直接存 user_id）
     */
    @TableField("created_by")
    @NotNull
    private User createdBy;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间（自动填充）
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

