package com.april.rag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2025/12/5 20:53
 * Description:
 * 文档向量实体类
 * 用于存储文本分块和相关元数据
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("document_vectors")
public class DocumentVector {

    @TableId(value = "vector_id", type = IdType.AUTO)
    private Long vectorId;

    @TableField(value = "file_md5")
    @NotNull
    @Size(max=32)
    private String fileMd5;

    @TableField(value = "chunk_id")
    @NotNull
    private Integer chunkId;

    /**
     * text_content 字段在数据库中为 LOB 类型
     * MyBatis-Plus 会根据数据库驱动自动处理大文本字段
     */
    @TableField(value = "text_content")
    private String textContent;

    @TableField(value = "model_version")
    @Size(max=32)
    private String modelVersion;

    /**
     * 上传用户ID
     */
    @TableField(value = "user_id")
    @NotNull
    @Size(max=64)
    private String userId;

    /**
     * 文件所属组织标签
     */
    @TableField(value = "org_tag")
    @Size(max=50)
    private String orgTag;

    /**
     * 文件是否公开
     * 注意：字段名与数据库列名不完全匹配，需要特殊处理
     */
    @TableField(value = "is_public")
    @NotNull
    private Boolean isPublic = false;
}