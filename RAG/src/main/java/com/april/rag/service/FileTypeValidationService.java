package com.april.rag.service;

import com.april.rag.model.FileTypeValidationResult;

import java.util.Set;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/15 13:12
 * Description:文件验证服务
 */

public interface FileTypeValidationService {

    /**
     * 验证文件类型是否支持
     *
     * @param fileName 文件名
     * @return 验证结果
     */
    FileTypeValidationResult validateFileType(String fileName);

    /**
     * 获取支持的文件类型列表（用于前端显示）
     *
     * @return 支持的文件类型描述列表
     */
    Set<String> getSupportedFileTypes();

    /**
     * 获取支持的文件扩展名列表
     *
     * @return 支持的文件扩展名集合
     */
    Set<String> getSupportedExtensions();
}
