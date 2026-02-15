package com.april.rag.service;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/15 14:20
 * Description:
 */

public interface VectorizationService {
    /**
     * 执行向量化操作
     * @param fileMd5 文件指纹
     * @param userId 上传用户ID
     * @param orgTag 组织标签
     * @param isPublic 是否公开
     */
    void vectorize(String fileMd5, String userId, String orgTag, boolean isPublic);
}
