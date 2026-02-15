package com.april.rag.service;


import com.april.rag.model.SearchResult;

import java.util.List;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/12 20:57
 * Description:混合搜索服务，结合文本匹配和向量相似度搜索
 * 支持权限过滤，确保用户只能搜索其有权限访问的文档
 */

public interface HybridSearchService {

    /**
     * 使用文本匹配和向量相似度进行混合搜索，支持权限过滤
     * 该方法确保用户只能搜索其有权限访问的文档（自己的文档、公开文档、所属组织的文档）
     *
     * @param query  查询字符串
     * @param userId 用户ID
     * @param topK   返回结果数量
     * @return 搜索结果列表
     */
    List<SearchResult> searchWithPermission(String query, String userId, int topK);

    /**
     * 原始搜索方法，不包含权限过滤，保留向后兼容性
     */
    List<SearchResult> search(String query, int topK);
}
