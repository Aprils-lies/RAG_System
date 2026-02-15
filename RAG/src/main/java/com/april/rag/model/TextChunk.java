package com.april.rag.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/15 14:23
 * Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextChunk {
    // Getters/Setters
    private int chunkId;       // 分块序号
    private String content;    // 分块内容
}
