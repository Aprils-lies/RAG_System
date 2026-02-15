package com.april.rag.service;

import org.apache.tika.exception.TikaException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/12 20:33
 * Description: 文本分块
 */

public interface ParseService {

    /**
     * 以流式方式解析文件，将内容分块并保存到数据库，以避免OOM。
     * 采用"父文档-子切片"策略。
     *
     * @param fileMd5    文件的MD5哈希值，用于唯一标识文件
     * @param fileStream 文件输入流，用于读取文件内容
     * @param userId     上传用户ID
     * @param orgTag     组织标签
     * @param isPublic   是否公开
     * @throws IOException   如果文件读取过程中发生错误
     * @throws TikaException 如果文件解析过程中发生错误
     */
    void parseAndSave(String fileMd5, InputStream fileStream,
                             String userId, String orgTag, boolean isPublic) throws IOException, TikaException;

    /**
     * 兼容旧版本的解析方法
     */
    void parseAndSave(String fileMd5, InputStream fileStream) throws IOException, TikaException;

}
