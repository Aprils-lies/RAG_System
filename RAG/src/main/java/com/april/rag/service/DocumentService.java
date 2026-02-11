package com.april.rag.service;


import com.april.rag.entity.FileUpload;

import java.util.List;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/11 21:06
 * Description:文档管理服务类
 * 负责文档的删除等管理操作
 */

public interface DocumentService {

    /**
     * 删除文档及其相关数据
     * 该方法将删除:
     * 1. FileUpload记录
     * 2. DocumentVector记录
     * 3. MinIO中的文件
     * 4. Elasticsearch中的向量数据
     *
     * @param fileMd5 文件MD5
     */
    void deleteDocument(String fileMd5, String userId);

    /**
     * 获取用户可访问的所有文件列表
     * 包括用户自己的文件、公开文件和用户所属组织的文件（支持层级权限）
     *
     * @param userId 用户ID
     * @param orgTags 用户所属的组织标签（逗号分隔的字符串，仅供兼容性使用）
     * @return 用户可访问的文件列表
     */
    List<FileUpload> getAccessibleFiles(String userId, String orgTags);

    /**
     * 获取用户上传的所有文件列表
     *
     * @param userId 用户ID
     * @return 用户上传的文件列表
     */
    List<FileUpload> getUserUploadedFiles(String userId);

    /**
     * 生成文件下载链接
     *
     * @param fileMd5 文件MD5
     * @return 预签名下载URL
     */
    String generateDownloadUrl(String fileMd5);

    /**
     * 获取文件预览内容
     *
     * @param fileMd5 文件MD5
     * @param fileName 文件名
     * @return 文件预览内容，对于文本文件返回前几KB内容，非文本文件返回文件信息
     */
    String getFilePreviewContent(String fileMd5, String fileName);
}
