package com.april.rag.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import org.apache.http.ConnectionClosedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/7 21:16
 * Description:Elasticsearch索引自动初始化器
 */

@Component
public class EsIndexInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(EsIndexInitializer.class);

    @Autowired
    private ElasticsearchClient esClient;

    @Value("classpath:es-mappings/knowledge_base.json") // 加载 JSON 文件
    private Resource knowledgeBaseJson;

    /**
     * 初始化索引的核心逻辑
     * @throws Exception
     */
    private void initializeIndex() throws Exception {
        // 检查索引是否存在
        BooleanResponse existsResponse = esClient.indices().exists(ExistsRequest.of(e -> e.index("knowledge_base")));
        if (!existsResponse.value()) {
            // 读取 JSON 文件内容
            String mappingJson = new String(Files.readAllBytes(knowledgeBaseJson.getFile().toPath()), StandardCharsets.UTF_8);

            // 创建索引并应用映射
            CreateIndexRequest createIndexRequest = CreateIndexRequest.of(c -> c
                    .index("knowledge_base") // 索引名称
                    .withJson(new StringReader(mappingJson)) // 使用 JSON 文件定义映射
            );
            esClient.indices().create(createIndexRequest);
            logger.info("索引 'knowledge_base' 已创建");
        } else {
            logger.info("索引 'knowledge_base' 已存在");
        }
    }

    @Override
    public void run(String... args) {
        try {
            initializeIndex();
        } catch (Exception exception) {
            if (exception instanceof ConnectionClosedException || (exception.getCause() != null && exception.getCause() instanceof ConnectionClosedException)) {
                logger.error("Elasticsearch连接已关闭，等待5秒后重试...");
                try {
                    Thread.sleep(5000);
                    initializeIndex();
                } catch (Exception retryException) {
                    logger.error("Elasticsearch连接失败（重试后），应用将继续运行，但搜索功能不可用: {}", retryException.getMessage());
                }
            } else {
                logger.error("Elasticsearch索引初始化失败，应用将继续运行，但搜索功能不可用: {}", exception.getMessage());
            }
        }
    }
}
