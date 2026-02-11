package com.april.rag.service;

import com.april.rag.model.EsDocument;

import java.util.List;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/6 19:47
 * Description:
 */

public interface ElasticsearchService {
    /**
     * 批量索引文档到Elasticsearch中
     * 通过接收一个EsDocument对象列表，将这些文档批量索引到名为"knowledge_base"的索引中
     * 使用Elasticsearch的Bulk API来执行批量索引操作，以提高索引效率
     *
     * @param documents 文档列表，每个文档都将被索引到Elasticsearch中
     */
    void bulkIndex(List<EsDocument> documents);

    /**
     * 根据file_md5删除文档
     * @param fileMd5 文件指纹
     */
    void deleteByFileMd5(String fileMd5);
}
