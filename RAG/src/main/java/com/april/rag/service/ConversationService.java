package com.april.rag.service;

import com.april.rag.entity.Conversation;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/15 21:49
 * Description:对话管理
 */

public interface ConversationService {

    /**
     * 记录用户的对话历史。
     *
     * @param username 用户名
     * @param question 用户提问内容
     * @param answer 系统回答内容
     */
    void recordConversation(String username, String question, String answer);

    /**
     * 查询用户的对话历史。
     *
     * @param username 用户名
     * @param startDate 起始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 符合条件的对话记录列表
     */
    List<Conversation> getConversations(String username, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 管理员查询所有用户的对话历史。
     *
     * @param adminUsername 管理员用户名
     * @param targetUsername 目标用户名（可选，如果提供则只查询该用户的对话历史）
     * @param startDate 起始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 符合条件的对话记录列表
     */
    List<Conversation> getAllConversations(String adminUsername, String targetUsername,
                                                  LocalDateTime startDate, LocalDateTime endDate);
}
