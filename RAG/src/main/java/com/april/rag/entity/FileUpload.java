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
 * @CreateTime: 2025/12/5 20:53
 * Description:
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("file_upload")
public class FileUpload {

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件的唯一标识符（MD5）
     */
    @TableField("file_md5")
    @NotNull
    @Size(max = 32)
    private String fileMd5;

    /**
     * 文件原始名称
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件总大小（字节）
     */
    @TableField("total_size")
    private long totalSize;

    /**
     * 上传状态：0-上传中 1-已完成
     */
    @TableField("status")
    private int status;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @NotNull
    @Size(max = 64)
    private String userId;

    /**
     * 文件所属组织标签
     */
    @TableField("org_tag")
    private String orgTag;

    /**
     * 是否公开
     */
    @TableField("is_public")
    @NotNull
    private boolean isPublic;

    /**
     * 创建（上传）时间
     * INSERT 时自动填充
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 合并完成时间
     * INSERT_UPDATE 时自动填充
     */
    @TableField(value = "merged_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime mergedAt;
}
