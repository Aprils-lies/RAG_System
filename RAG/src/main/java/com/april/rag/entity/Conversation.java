package com.april.rag.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/15 21:51
 * Description:
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("conversations")
public class Conversation {

    /**
     * 对话记录唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户提问内容
     */
    @TableField("question")
    private String question;

    /**
     * 系统回答
     */
    @TableField(value = "answer")
    private String answer;

    /**
     * 对话时间
     */
    @TableField(value = "timestamp", fill = FieldFill.INSERT)
    private LocalDateTime timestamp;

}
