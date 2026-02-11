package com.april.rag.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/6 19:33
 * Description: ES配置
 */

@Configuration
public class EsConfig {
    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;

    @Value("${elasticsearch.scheme:https}")
    private String scheme;

    @Value("${elasticsearch.username:elastic}")
    private String username;

    @Value("${elasticsearch.password:changeme}")
    private String password;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 创建低级客户端
        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, scheme));

        //  设置基本认证
        if(username != null && password != null) {
            BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                // 忽略TLS证书 (仅开发环境)
                try {
                    SSLContext sslContext = SSLContexts.custom()
                            .loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true)
                            .build();
                    httpClientBuilder.setSSLContext(sslContext);
                    httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                } catch (Exception e) {
                    // ignore
                }
                return httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
            });
        }

        RestClient restClient = builder.build();

        // 创建传输层
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper()
        );

        // 返回高级客户端
        return new ElasticsearchClient(transport);
    }

}
