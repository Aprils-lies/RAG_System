# RAG_System

智能知识库 RAG（Retrieval-Augmented Generation）系统，实现文档上传、解析分块、向量化索引、混合检索与 AI 智能问答的全链路闭环。

## 系统架构

```text
┌─────────────────────────────────────────────────────────┐
│                    客户端 (WebSocket)                      │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                     Spring Security                       │
│              JWT 认证 + 组织标签权限过滤                    │
└──────┬──────────────────────────────┬───────────────────┘
       │                              │
┌──────▼──────────┐     ┌────────────▼──────────────────┐
│  WebSocket 聊天   │     │      REST API 控制器层           │
│  (流式 AI 回复)   │     │  Chat/Upload/Doc/Search/Admin │
└──────┬──────────┘     └────────────┬───────────────────┘
       │                              │
┌──────▼──────────────────────────────▼───────────────────┐
│                     服务层 (Service)                      │
│  ChatHandler  │  HybridSearch  │  Upload  │  Document   │
│  Vectorization│  Parse         │  TokenCache  │  OrgTag   │
└──────┬──────────────────────────────┬───────────────────┘
       │                              │
┌──────▼──────────┐     ┌────────────▼──────────────────┐
│  外部 AI 客户端    │     │        Kafka 异步管道           │
│ DeepSeek(对话)   │     │  文件解析 → 向量化 → ES 索引    │
│ DashScope(嵌入)  │     └────────────────────────────────┘
└─────────────────┘
```

## 技术栈

| 层级 | 技术 |
|------|------|
| **核心框架** | Spring Boot 3.4.2, Spring MVC, Spring Security, Spring WebFlux |
| **ORM** | MyBatis-Plus 3.5.15 |
| **数据库** | MySQL 8.x |
| **搜索引擎** | Elasticsearch 8.10 (KNN 向量检索 + BM25 全文检索) |
| **向量嵌入** | 阿里云 DashScope text-embedding-v4 (2048维) |
| **AI 对话** | DeepSeek API (deepseek-chat), WebSocket 流式传输 |
| **对象存储** | MinIO (文件分块存储与合并) |
| **缓存** | Redis (JWT 黑名单、上传位图、对话历史、组织标签缓存) |
| **消息队列** | Apache Kafka (异步文件处理管道) |
| **文件解析** | Apache Tika 2.9.1 (自动文档解析) |
| **中文 NLP** | HanLP 1.8.6 (语义分句) |
| **认证授权** | JWT (jjwt 0.11.5), BCrypt, 组织标签多租户权限 |
| **构建工具** | Maven, Java 17 |

## 快速开始

### 前置依赖

- JDK 17+
- Maven 3.6+
- Docker (用于 ES、MinIO、Kafka、Zookeeper)
- MySQL 8.0+
- Redis

### 启动基础设施

```bash
docker-compose up -d
```

### 配置

`src/main/resources/application.yml` 中的关键配置：

```yaml
# AI 服务
deepseek:
  api:
    url: https://api.deepseek.com/v1
    model: deepseek-chat
    key: your-api-key

embedding:
  api:
    url: https://dashscope.aliyuncs.com/compatible-mode/v1
    key: your-api-key
    model: text-embedding-v4
    dimension: 2048
```

### 运行

```bash
cd RAG
mvn spring-boot:run
```

应用启动后访问 `http://localhost:8081/test.html`。

## 核心工作流

### 1. 文件上传与处理管道

```text
客户端分块上传 ──→ MinIO 存储分块 ──→ 合并 ──→ Kafka 消息
                                                 │
                                          ┌──────▼──────┐
                                          │ 异步消费者    │
                                          │              │
                                    ┌─────▼─────┐  ┌────▼─────┐
                                    │ Tika 解析   │  │ 向量化    │
                                    │ (流式分块)   │  │ (DashScope)│
                                    └─────┬─────┘  └────┬─────┘
                                          │              │
                                          └──────┬──────┘
                                                 ▼
                                        Elasticsearch 知识库
```

- **分块上传**：5MB 每块，Redis 位图追踪上传进度
- **合并**：MinIO `composeObject` 服务端合并
- **异步处理**：Kafka 事务消息保证精确一次语义
- **文档解析**：Apache Tika 流式解析 + HanLP 语义分句
- **向量化**：DashScope 嵌入后批量索引到 ES

### 2. RAG 智能问答

```text
用户提问 ──→ WebSocket ──→ 混合检索 ──→ 构建 Prompt ──→ DeepSeek 流式回复
                              │
                    ┌─────────┼─────────┐
                    ▼         ▼         ▼
                 KNN 语义  BM25 关键词  权限过滤
```

## REST API

### 用户认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/users/register` | 注册 |
| POST | `/api/v1/users/login` | 登录 |
| POST | `/api/v1/users/logout` | 登出 |
| POST | `/api/v1/users/logout-all` | 全部设备登出 |
| GET | `/api/v1/users/me` | 当前用户信息 |

### 文件上传

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/upload/chunk` | 上传文件分块 |
| POST | `/api/v1/upload/merge` | 合并分块 |
| GET | `/api/v1/documents/accessible` | 可访问文件列表 |
| DELETE | `/api/v1/documents/{fileMd5}` | 删除文档 |
| GET | `/api/v1/documents/download` | 下载（预签名 URL） |
| GET | `/api/v1/documents/preview` | 预览（文本前 10KB） |

### 搜索

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/search/hybrid` | 混合搜索（KNN + BM25 + 权限过滤） |

### 聊天

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/chat/websocket-token` | 获取 WebSocket 连接令牌 |
| WebSocket | `/chat/{token}` | 流式 AI 对话 |

### 管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/users` | 用户列表 |
| GET | `/api/v1/admin/system/status` | 系统状态 |
| POST | `/api/v1/admin/org-tags` | 创建组织标签 |
| GET | `/api/v1/admin/org-tags/tree` | 组织标签树 |

## WebSocket 消息格式

### 发送消息

```json
{
  "type": "message",
  "content": "合同违约条款是什么？",
  "_internal_cmd_token": "stop-token"
}
```

### 接收回复

```json
{"chunk": "根据检索到的资料，"}
{"chunk": "合同第3条明确规定了违约"}
{"chunk": "责任..."}
{"type": "completion", "status": "finished", "message": "响应已完成"}
```

### 停止响应

```json
{
  "type": "stop",
  "_internal_cmd_token": "之前获取到的令牌"
}
```

## 权限体系

系统支持三级数据隔离：

1. **个人文档**：仅上传者可见
2. **公开文档**：所有用户可见
3. **组织文档**：同组织标签及子标签用户可见

组织标签支持层级结构（`parentTag`），自动解析所有祖先标签计算有效权限。

## 项目亮点

- **混合搜索**：ES KNN 语义检索 + BM25 关键词匹配两阶段召回，Rescore 重排序，向量服务不可用时自动降级到纯文本搜索
- **流式文档解析**：Apache Tika StreamingContentHandler 防止大文件 OOM，配合 HanLP 实现中文语义分句
- **Redis 位图追踪**：每个分块 1 bit，1GB 文件仅需 25 字节跟踪上传进度
- **Kafka 异步管道**：文件合并后异步解析 + 向量化，生产者幂等 + 事务保证精确一次语义，自动死信队列兜底
- **JWT 优雅刷新**：令牌过期后 10 分钟内仍可刷新，响应头主动预下发新令牌
- **多租户权限**：组织标签层级继承，检索层面直接拼装权限过滤条件

## 开发计划 / 可扩展方向

- [ ] 检索后 Rerank（引入 Cross-Encoder 精排）
- [ ] 查询改写（Query Rewrite / Query Expansion）
- [ ] 来源溯源校验（AI 回答与原文自动比对）
- [ ] 用户反馈闭环（点赞/点踩）
- [ ] 流式解析父文档-子切片策略优化
- [ ] 管理员统计面板

## 环境要求

| 服务 | 端口 |
|------|------|
| Spring Boot 应用 | 8081 |
| Elasticsearch | 9200, 9300 |
| MinIO API | 9000 |
| MinIO Console | 9001 |
| Kafka | 9092 |
| Zookeeper | 2181 |
